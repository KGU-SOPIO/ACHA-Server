package sopio.acha.domain.member.domain;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import lombok.NoArgsConstructor;
import sopio.acha.common.handler.EncryptionHandler;
import sopio.acha.domain.member.presentation.dto.MemberDto;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "member")
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
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
    private Role role;

    @Timestamp
    private LocalDateTime created_date;

    public boolean chkPassword(String password, Member member, BCryptPasswordEncoder bCryptPasswordEncoder) {
        return bCryptPasswordEncoder.matches(password, member.getPassword());
    }

    public Member(String id, String password, String name, String college, String department, String major, String role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.college = college;
        this.department = department;
        this.major = major;
        this.role = Role.valueOf(role);
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

    public static Member create(String id, String password, String name, String college, String department, String major) {
        return Member.builder()
                .id(id)
                .password(EncryptionHandler.encrypt(password))
                .name(name)
                .college(college)
                .department(department)
                .major(major)
                .role(Role.ROLE_USER)
                .build();
    }

}
