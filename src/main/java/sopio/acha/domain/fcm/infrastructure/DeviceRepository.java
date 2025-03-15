package sopio.acha.domain.fcm.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import sopio.acha.domain.fcm.domain.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {
	boolean existsByDeviceToken(String deviceToken);

	Optional<Device> findByMemberIdAndDeviceToken(String memberId, String deviceToken);
}
