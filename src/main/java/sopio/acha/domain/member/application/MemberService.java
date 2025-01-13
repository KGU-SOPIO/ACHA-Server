package sopio.acha.domain.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import sopio.acha.common.api.RestTemplateService;
import sopio.acha.common.api.dto.ResponseDto;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.dto.MemberDto;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final RestTemplateService restTemplateService;

    public MemberDto join(String id, String password) {
        Member member = memberRepository.findMemberById(id);

        if (member == null) {
            ResponseDto responseDto = restTemplateService.isExtract(id, password, true);

            if (responseDto.isVerification()) {
                MemberDto memberDto = new MemberDto();

                memberDto.setId(id);
                memberDto.setPassword(password);
                memberDto.setName(responseDto.getUserData().getName());
                memberDto.setCollege(responseDto.getUserData().getCollege());
                memberDto.setDepartment(responseDto.getUserData().getDepartment());
                memberDto.setMajor(responseDto.getUserData().getMajor());
                memberDto.setRole("ROLE_USER");

                Member newMember = Member.of(memberDto, bCryptPasswordEncoder);

                memberRepository.save(newMember);
                return memberDto;
            }
            else {
                throw new IllegalArgumentException("인증 실패: " + responseDto.getMessage());
            }
        }
        return MemberDto.of(member);
    }

    public Member me(){
        return memberRepository.findMemberById("202211516"); // 임시로 고정, 추후 SecurityContextHolder 에서 가져와야함
    }
}
