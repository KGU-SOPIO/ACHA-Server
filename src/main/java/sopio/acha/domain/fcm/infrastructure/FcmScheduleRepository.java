package sopio.acha.domain.fcm.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.fcm.domain.FcmSchedule;

@Repository
public interface FcmScheduleRepository extends JpaRepository<FcmSchedule, Long> {
	List<FcmSchedule> findAllBySendTimeBeforeAndMember_AlertIsTrue(LocalDateTime now);
}
