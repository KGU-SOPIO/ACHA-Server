package sopio.acha.common.auth.application;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.domain.Member;


@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {
    private final MemberService memberService;

    @Override
    public Member loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberService.getMemberById(username);
    }
}
