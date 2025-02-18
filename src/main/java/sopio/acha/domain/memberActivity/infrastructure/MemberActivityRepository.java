package sopio.acha.domain.memberActivity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sopio.acha.domain.memberActivity.domain.MemberActivity;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity, Long> {
}
