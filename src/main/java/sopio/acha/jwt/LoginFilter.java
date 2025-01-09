package sopio.acha.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sopio.acha.domain.member.presentation.dto.CustomUserDetails;
import sopio.acha.domain.member.presentation.dto.MemberDto;
import sopio.acha.jwt.domain.RefreshEntity;
import sopio.acha.jwt.infrastructure.RefreshRepository;

import java.util.Date;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTCreator jwtCreator;

    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTCreator jwtCreator, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtCreator = jwtCreator;
        this.refreshRepository = refreshRepository;
        setFilterProcessesUrl("/member/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String id = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(id, password, null);

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter("id");
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authen) {
        System.out.println("success");
        CustomUserDetails customUserDetails = (CustomUserDetails) authen.getPrincipal();
        MemberDto memberDto = customUserDetails.getMemberDto();

        // access token 10분
        String access = jwtCreator.createJwt("access", memberDto, 600000L);
        // refresh token 1일
        String refresh = jwtCreator.createJwt("refresh", memberDto, 86400000L);

        addRefreshEntity(refresh, memberDto.getId(), 86400000L);

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("failed");

        try {
            String errorMessage;
            if (failed.getMessage().contains("Bad credentials")) {
                errorMessage = "아이디 또는 비밀번호가 잘못되었습니다.";
            } else {
                errorMessage = "인증에 실패했습니다. 다시 시도해주세요.";
            }

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");

            String jsonResponse = "{\"error\": \"Authentication failed\", \"message\": \"" + errorMessage + "\"}";
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshEntity(String refresh, String username, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setRefresh(refresh);
        refreshEntity.setUsername(username);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}
