package sopio.acha.common.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.filter.JwtFilter;
import sopio.acha.common.auth.jwt.JwtCreator;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
	private final JwtCreator jwtCreator;

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new JwtFilter(jwtCreator), UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(SWAGGER_PATTERNS).permitAll()
						.requestMatchers(STATIC_RESOURCES_PATTERNS).permitAll()
						.requestMatchers(PERMIT_ALL_PATTERNS).permitAll()
						.anyRequest().permitAll())
				.build();
	}

	private static final String[] SWAGGER_PATTERNS = {
			"/swagger-ui/**",
			"/actuator/**",
			"/v3/api-docs/**",
	};

	private static final String[] STATIC_RESOURCES_PATTERNS = {
			"/img/**",
			"/css/**",
			"/js/**"
	};

	private static final String[] PERMIT_ALL_PATTERNS = {
			"/api/v1/",
			"/member/**",
			"/reissue",
			"/api/v1/lecture",
			"/error",
			"/favicon.ico",
			"/index.html",
			"/",
	};

	CorsConfigurationSource corsConfigurationSource() {
		return _ -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowedHeaders(Collections.singletonList("*"));
			config.setAllowedMethods(Collections.singletonList("*"));
			config.setAllowedOriginPatterns(List.of("https://api.sopio.kr", "http://localhost:3000",
					"https://prod.sopio.kr", "https://acha.sopio.kr"));
			config.setAllowCredentials(true);
			return config;
		};
	}
}
