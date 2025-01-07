package sopio.acha.common.api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseDto {

    private boolean verification;

    private UserData userData;

    private String message;

    @Getter
    @Setter
    public static class UserData {
        private String name;

        private String college;

        private String department;

        private String major;
    }

}
