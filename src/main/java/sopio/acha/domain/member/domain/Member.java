package sopio.acha.domain.member.domain;

import static lombok.AccessLevel.PROTECTED;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

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
import sopio.acha.domain.member.presentation.exception.PasswordNotMatchedException;

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

	public static Member create(String id, String password, String name, String college, String department,
		String major) {
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

	public void validatePassword(String password) {
		if (!Objects.equals(EncryptionHandler.encrypt(password), this.password)) {
			throw new PasswordNotMatchedException();
		}
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
