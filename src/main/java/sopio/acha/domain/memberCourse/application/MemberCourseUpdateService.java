package sopio.acha.domain.memberCourse.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.common.utils.CourseDataConverter;
import sopio.acha.domain.activity.application.ActivityService;
import sopio.acha.domain.activity.infrastructure.ActivityRepository;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingResponse;
import sopio.acha.domain.activity.presentation.response.ActivityScrapingWeekResponse;
import sopio.acha.domain.course.application.CourseService;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.infrastructure.CourseRepository;
import sopio.acha.domain.course.presentation.response.CourseScrapingResponse;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.memberCourse.domain.MemberCourse;
import sopio.acha.domain.memberCourse.infrastructure.MemberCourseRepository;
import sopio.acha.domain.timetable.presentation.response.TimetableScrapingResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.*;
import static sopio.acha.common.handler.ExtractorHandler.requestTimetable;

@Service
@RequiredArgsConstructor
public class MemberCourseUpdateService {
    private final MemberCourseRepository memberCourseRepository;
    private final ActivityRepository activityRepository;
    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final ActivityService activityService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMemberCoursesForMember(Member member, List<MemberCourse> memberCourseList, ObjectMapper objectMapper) {
        String decryptedPassword = decrypt(member.getPassword());

        // 추출 가능 상태 검증 및 실패 시 모든 강좌 업데이트 대기 적용
        try {
            requestAuthentication(member.getId(), decryptedPassword);
        } catch (ExtractorErrorException e) {
            memberCourseList
                    .forEach(memberCourse -> memberCourse.setLastUpdatedAt(LocalDateTime.now().plusHours(3)));
            return;
        }

        // 강좌 목록 요청 및 변환
        List<CourseScrapingResponse> lmsCourseList = fetchCourseListResponse(member, decryptedPassword,
                objectMapper);
        if (lmsCourseList == null || lmsCourseList.isEmpty()) {
            return;
        }

        // 신규 강좌 저장 및 결과 반환
        Map<String, CourseScrapingResponse> lmsCourseMap = CourseDataConverter.mapCourseByIdentifier(lmsCourseList);
        boolean isNewCourseSaved = courseService.saveNewCourses(lmsCourseMap);

        // 시간표 데이터 요청 및 강좌 매핑
        if (isNewCourseSaved) {
            // 시간표 데이터 요청 및 변환
            List<TimetableScrapingResponse> timetableList = fetchTimetableListResponse(member, decryptedPassword,
                    objectMapper);
            if (timetableList == null || timetableList.isEmpty())
                return;
            Map<String, List<TimetableScrapingResponse>> timetableMap = CourseDataConverter
                    .mapTimetableByIdentifier(timetableList);

            // 시간표 데이터 저장
            courseService.updateCourseWithTimetable(timetableMap);

            // Member - Course 매핑
            courseService.saveCoursesWithMember(lmsCourseList, member);
        }

        // LMS에 존재하지 않는 강의 삭제
        Set<String> lmsIdentifiers = lmsCourseList.stream()
                .map(CourseScrapingResponse::identifier)
                .collect(Collectors.toSet());
        Set<String> identifiersToRemove = memberCourseList.stream()
                .map(memberCourse -> memberCourse.getCourse().getIdentifier())
                .collect(Collectors.toSet());
        identifiersToRemove.removeAll(lmsIdentifiers);
        Iterator<MemberCourse> iterator = memberCourseList.iterator();
        while (iterator.hasNext()) {
            MemberCourse memberCourse = iterator.next();
            if (identifiersToRemove.contains(memberCourse.getCourse().getIdentifier())) {
                // 활동 삭제
                activityRepository.deleteAllByMemberAndCourse(member, memberCourse.getCourse());
                // MemberCourse 삭제
                memberCourseRepository.delete(memberCourse);
                iterator.remove();
                System.out.println("[ Scheduled ] 사용자 MemberCourse 삭제");
            }
        }

        // 강좌 상세 데이터 요청 및 공지사항, 활동 업데이트
        for (MemberCourse memberCourse : memberCourseList) {
            Course course = memberCourse.getCourse();

            // 강좌 데이터 요청
            CourseScrapingResponse detailedResponse = fetchCourseDetailResponse(member, decryptedPassword,
                    course.getCode(), objectMapper);
            if (detailedResponse == null)
                continue;

            // 공지사항 업데이트 및 저장
            Map<String, CourseScrapingResponse> detailedCourseMap = CourseDataConverter
                    .mapCourseByIdentifier(Collections.singletonList(detailedResponse));
            courseService.extractAndSaveNotification(detailedCourseMap);

            // 활동 업데이트 및 저장
            List<ActivityScrapingWeekResponse> weekResponses = detailedResponse.activities();
            if (weekResponses != null && !weekResponses.isEmpty()) {
                for (ActivityScrapingWeekResponse weekResponse : weekResponses) {
                    int week = weekResponse.week();
                    for (ActivityScrapingResponse activityResponse : weekResponse.activities()) {
                        activityService.saveOrUpdateActivity(course, member, week, activityResponse);
                    }
                }
            }
        }
    }

    /// 사용자의 강좌 목록 데이터를 요청합니다.
    /// 공지사항, 활동 데이터는 포함되지 않습니다.
    private List<CourseScrapingResponse> fetchCourseListResponse(Member member, String decryptedPassword,
                                                                 ObjectMapper objectMapper) {
        try {
            JsonNode courseListJsonData = objectMapper.readTree(requestCourseList(member.getId(), decryptedPassword))
                    .get("data");
            return CourseDataConverter
                    .convertToCourseList(objectMapper, courseListJsonData);
        } catch (Exception e) {
            return null;
        }
    }

    /// 사용자의 특정 강좌 데이터를 요청합니다.
    private CourseScrapingResponse fetchCourseDetailResponse(Member member, String decryptedPassword, String courseCode,
                                                             ObjectMapper objectMapper) {
        try {
            JsonNode courseDetailJsonData = objectMapper
                    .readTree(requestCourseDetail(member.getId(), decryptedPassword, courseCode)).get("data");
            return objectMapper.convertValue(courseDetailJsonData, CourseScrapingResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    /// 시간표 데이터를 요청합니다.
    private List<TimetableScrapingResponse> fetchTimetableListResponse(Member member, String decryptedPassword,
                                                                       ObjectMapper objectMapper) {
        try {
            JsonNode timetableJsonData = objectMapper
                    .readTree(requestTimetable(member.getId(), decryptedPassword)).get("data");
            return CourseDataConverter.convertToTimetableList(objectMapper, timetableJsonData);
        } catch (Exception e) {
            return null;
        }
    }
}
