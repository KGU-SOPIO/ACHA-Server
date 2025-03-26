package sopio.acha.common.exception;

import static sopio.acha.common.exception.GlobalExceptionCode.KUTIS_PASSWORD_ERROR;

public class KutisPasswordErrorException extends CustomException{
    public KutisPasswordErrorException(){super(KUTIS_PASSWORD_ERROR); }

}
