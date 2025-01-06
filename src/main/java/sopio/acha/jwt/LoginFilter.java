package sopio.acha.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sopio.acha.domain.member.presentation.dto.CustomUserDetails;
import sopio.acha.domain.member.presentation.dto.MemberDto;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTCreator jwtCreator;

    public LoginFilter(AuthenticationManager authenticationManager, JWTCreator jwtCreator) {
        this.authenticationManager = authenticationManager;
        this.jwtCreator = jwtCreator;
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

        // access token 10시간 후 만료
        String token = jwtCreator.createJwt(memberDto, 60 * 60 * 10L);

        response.addHeader("Authorization", "Bearer " + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("failed");
        response.setStatus(401);
    }
}
