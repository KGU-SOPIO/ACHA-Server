package sopio.acha.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.presentation.dto.MemberDto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTCreator {

    private final SecretKey secretKey;

    public JWTCreator(@Value("${spring.jwt.secret}") String secret) {
        // 양방향 대칭키, HMAC 알고리즘(HS256)
        // AES-256 변경??
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createJwt(String category, MemberDto memberDto, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("id", memberDto.getId())
                .claim("password", memberDto.getPassword())
                .claim("name", memberDto.getName())
                .claim("college", memberDto.getCollege())
                .claim("department", memberDto.getDepartment())
                .claim("major", memberDto.getMajor())
                .claim("role", memberDto.getRole())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public <T> T getClaim(String token, String key, Class<T> classType) {
        return extractAllClaims(token).get(key, classType);
    }

    public Member getMember(String token) {
        String id = getClaim(token, "id", String.class);
        String password = getClaim(token, "password", String.class);
        String name = getClaim(token, "name", String.class);
        String college = getClaim(token, "college", String.class);
        String department = getClaim(token, "department", String.class);
        String major = getClaim(token, "major", String.class);
        String role = getClaim(token, "role", String.class);

        return new Member(
                id,
                password,
                name,
                college,
                department,
                major,
                role
        );
    }

    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
