package sopio.acha.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jfr.Timestamp;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import sopio.acha.domain.member.presentation.dto.MemberDto;

import java.time.LocalDateTime;

@Entity(name = "Member")
@Table(name = "member")
@Getter
public class Member {

    @Id
    @Column(nullable = false, updatable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String college;

    @Column(nullable = false)
    private String department;

    @Column
    private String major;

    @Column(nullable = false)
    private String role;

    @Timestamp
    private LocalDateTime created_date;

    public boolean chkPassword(String password, Member member, BCryptPasswordEncoder bCryptPasswordEncoder) {
        return bCryptPasswordEncoder.matches(password, member.getPassword());
    }

    public Member() {}

    public Member(String id, String password, String name, String college, String department, String major, String role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.college = college;
        this.department = department;
        this.major = major;
        this.role = role;
        this.created_date = LocalDateTime.now();
    }

    public static Member of(MemberDto memberDto, BCryptPasswordEncoder bCryptPasswordEncoder) {
        return new Member(
                memberDto.getId(),
                bCryptPasswordEncoder.encode(memberDto.getPassword()),
                memberDto.getName(),
                memberDto.getCollege(),
                memberDto.getDepartment(),
                memberDto.getMajor(),
                memberDto.getRole()
        );
    }

}
