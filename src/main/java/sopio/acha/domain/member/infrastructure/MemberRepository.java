package sopio.acha.domain.member.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sopio.acha.domain.member.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findMemberById(String id);

}
