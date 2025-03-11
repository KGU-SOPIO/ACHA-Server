package sopio.acha.domain.activity.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.activity.domain.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
	boolean existsActivityByTitleAndMemberId(String title, String memberId);
	List<Activity> findTop10ByMemberIdAndDeadlineAfterOrderByDeadlineAsc(String memberId, LocalDateTime now);
}
