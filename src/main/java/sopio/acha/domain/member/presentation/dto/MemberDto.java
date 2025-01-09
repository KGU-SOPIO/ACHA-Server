package sopio.acha.domain.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import sopio.acha.domain.member.domain.Member;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class MemberDto {

    @Schema(description = "학번", example = "202211403")
    private String id;

    @Schema(description = "비밀번호", example = "1234")
    private String password;

    @Schema(description = "이름", example = "권우진")
    private String name;

    @Schema(description = "단과대", example = "소프트웨어경영대학")
    private String college;

    @Schema(description = "학부", example = "컴퓨터공학부")
    private String department;

    @Schema(description = "학과", example = "컴퓨터공학과")
    private String major;

    @Schema(description = "권한", example = "ROLE_USER")
    private String role;

    public static MemberDto of(Member member) {
        return new ModelMapper().map(member, MemberDto.class);
    }

}
