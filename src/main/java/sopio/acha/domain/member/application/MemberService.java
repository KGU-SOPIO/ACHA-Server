package sopio.acha.domain.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.dto.MemberDto;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public MemberDto login(String id, String password) {
        Member member = memberRepository.findMemberById(id);

        if (member == null) {
            throw new IllegalArgumentException("아이디가 존재하지 않습니다.");
        }

        if (member.chkPassword(password, member, bCryptPasswordEncoder)) {
            return MemberDto.of(member);
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

    }

    public MemberDto join(MemberDto memberDto) {
        Member member = Member.of(memberDto, bCryptPasswordEncoder);

        memberRepository.save(member);
        return memberDto;
    }




}
