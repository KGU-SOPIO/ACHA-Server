package sopio.acha.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static sopio.acha.common.exception.GlobalExceptionCode.KUTIS_PASSWORD_ERROR;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class KutisPasswordErrorException extends CustomException{
    public KutisPasswordErrorException(){super(KUTIS_PASSWORD_ERROR); }

}
