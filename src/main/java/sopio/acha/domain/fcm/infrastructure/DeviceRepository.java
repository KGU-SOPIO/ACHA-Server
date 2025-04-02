package sopio.acha.domain.fcm.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.fcm.domain.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
	boolean existsByDeviceToken(String deviceToken);

	Optional<Device> findByMemberIdAndDeviceToken(String memberId, String deviceToken);

	Device findByMember_Id(String memberId);
}
