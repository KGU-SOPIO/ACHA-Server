package sopio.acha.domain.member.domain;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;
import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.EncryptionHandler.encrypt;
import static sopio.acha.domain.member.domain.Role.ROLE_USER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopio.acha.common.domain.BaseTimeEntity;
import sopio.acha.domain.fcm.domain.Device;
import sopio.acha.domain.member.presentation.exception.InvalidPasswordException;

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

	@ColumnDefault("true")
	private Boolean alert;

	@Column(nullable = false)
	@Enumerated(STRING)
	private Role role;

	@OneToMany(mappedBy = "member", cascade = ALL, fetch = LAZY)
	private List<Device> devices = new ArrayList<>();

	public static Member save(String id, String password, String name, String college, String department,
		String major) {
		return Member.builder()
			.id(id)
			.password(encrypt(password))
			.name(name)
			.college(college)
			.department(department)
			.major(major)
			.alert(true)
			.role(ROLE_USER)
			.build();
	}

	public void updatePassword(String password) {
		this.password = encrypt(password);
  }
  
	public void updateAlert(Boolean alert) {
		this.alert = alert;
	}

	public void updateBasicInformation(String name, String college, String department, String major) {
		this.name = name;
		this.college = college;
		this.department = department;
		this.major = major;
	}

	public void validatePassword(String password) {
		if (!Objects.equals(decrypt(this.password), password)) throw new InvalidPasswordException();
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
