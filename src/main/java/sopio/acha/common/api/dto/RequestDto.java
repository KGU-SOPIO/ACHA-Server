package sopio.acha.common.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class RequestDto {

    @JsonProperty("authentication")
    private Authen auth;

    @JsonProperty("user")
    private boolean user;

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Authen {
        @JsonProperty("studentId")
        private String studentId;

        @JsonProperty("password")
        private String password;
    }

}
