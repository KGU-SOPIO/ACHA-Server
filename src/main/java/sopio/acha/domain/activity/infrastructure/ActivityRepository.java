package sopio.acha.domain.activity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.activity.domain.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
	boolean existsActivityByTitleAndMemberId(String title, String memberId);
}
