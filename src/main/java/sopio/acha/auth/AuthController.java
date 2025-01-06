package sopio.acha.auth;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.presentation.dto.MemberDto;
import sopio.acha.jwt.JWTCreator;
import sopio.acha.jwt.infrastructure.RefreshRepository;

@RestController
public class AuthController {

    private final JWTCreator jwtCreator;

    private final RefreshRepository refreshRepository;

    public AuthController(JWTCreator jwtCreator, RefreshRepository refreshRepository) {
        this.jwtCreator = jwtCreator;
        this.refreshRepository = refreshRepository;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;
        Cookie[] cookies = request.getCookies();

        for(Cookie cookie : cookies) {
            System.out.println("Cookie name: " + cookie.getName());
            System.out.println("Cookie value: " + cookie.getValue());
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        try {
            jwtCreator.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        String category = jwtCreator.getClaim(refresh, "category", String.class);
        if (!category.equals("refresh")) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        Member member = jwtCreator.getMember(refresh);
        String newAccess = jwtCreator.createJwt("access", MemberDto.of(member), 600000L);

        response.setHeader("access", newAccess);

        return new ResponseEntity<>(HttpStatus.OK);

    }

}
