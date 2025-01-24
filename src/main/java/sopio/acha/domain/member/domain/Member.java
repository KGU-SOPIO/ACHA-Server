package sopio.acha.domain.member.domain;

import static lombok.AccessLevel.PROTECTED;
import static sopio.acha.domain.member.domain.Role.ROLE_USER;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.common.handler.EncryptionHandler;

@Getter
@Entity
@Builder
@Table(name = "member")
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Member extends BaseTimeEntity implements UserDetails {

	@Id
	@Column(nullable = false, updatable = false, unique = true)
	private String id;

	@Column(nullable = false)
	private String password;

	private String name;

	private String college;

	private String department;

	private String major;

	@Column(nullable = false)
	private Role role;

	public static Member createEmptyMember(String id, String password) {
		return Member.builder()
			.id(id)
			.password(password)
			.role(ROLE_USER)
			.build();
	}

	public void updateBasicInformation(String name, String college, String department, String major) {
		this.name = name;
		this.college = college;
		this.department = department;
		this.major = major;
	}

	public void updatePassword(String password) {
		this.password = EncryptionHandler.encrypt(password);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
	}

	@Override
	public String getUsername() {
		return id;
	}

	@Override
	public String getPassword() {
		return password;
	}

}
