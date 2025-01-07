package sopio.acha.common.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sopio.acha.common.api.dto.RequestDto;
import sopio.acha.common.api.dto.ResponseDto;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class RestTemplateService {

    private final RestTemplate restTemplate;

    private final String API_URL = "https://extractor.sopio.kr";

    public ResponseDto isExtract(String id, String password, Boolean isMember) {
        URI uri = UriComponentsBuilder
                .fromUriString(API_URL)
                .path("/auth/")
                .encode()
                .build()
                .toUri();

        RequestDto requestDto = new RequestDto();
        RequestDto.Authen authen = new RequestDto.Authen();
        authen.setStudentId(id);
        authen.setPassword(password);

        requestDto.setAuth(authen);
        requestDto.setUser(isMember);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RequestDto> entity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<ResponseDto> response = restTemplate.postForEntity(
                    uri, entity, ResponseDto.class
            );

            return response.getBody();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            ResponseDto responseDto = new ResponseDto();
            responseDto.setVerification(false);
            responseDto.setMessage(e.getMessage());
            return responseDto;
        }
    }

}
