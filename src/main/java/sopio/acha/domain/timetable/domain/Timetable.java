package sopio.acha.domain.timetable.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sopio.acha.domain.course.domain.Course;
import sopio.acha.domain.course.domain.CourseDay;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
public class Timetable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Setter
    private String lectureRoom;

    @Enumerated(STRING)
    private CourseDay day;

    @Formula("CASE WHEN day = '월요일' THEN 1 " +
            "WHEN day = '화요일' THEN 2 " +
            "WHEN day = '수요일' THEN 3 " +
            "WHEN day = '목요일' THEN 4 " +
            "WHEN day = '금요일' THEN 5 " +
            "ELSE 0 END")
    private int dayOrder;

    @Setter
    private int classTime;

    @Setter
    private int startAt;

    @Setter
    private int endAt;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    public void setCourse(Course course) {
        this.course = course;
    }
}
