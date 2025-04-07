package sopio.acha.domain.member.application;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.presentation.exception.MemberNotAuthenticatedException;

@Service
@RequiredArgsConstructor
public class CurrentMemberService {
	private final MemberService memberService;

	public Member me() {
		try {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String userId = ((UserDetails) principal).getUsername();
			return memberService.getMemberById(userId);
		} catch (Exception e) {
			throw new MemberNotAuthenticatedException();
		}
	}
}
