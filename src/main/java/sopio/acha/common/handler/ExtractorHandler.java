package sopio.acha.common.handler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.exception.ConvertErrorException;

@Component
@RequiredArgsConstructor
public class ExtractorHandler {

	@Value("${extractor.request-url}")
	private String requestUrlInstance;

	private static String requestUrl;

	@PostConstruct
	public void init() {
		requestUrl = requestUrlInstance;
	}

	public static List<Object> requestTimeTable(String studentId, String password) {
		try {
			URI uri = UriComponentsBuilder
				.fromUriString(requestUrl)
				.path("/timetable/")
				.encode()
				.build()
				.toUri();

			String requestBody = "{ \"studentId\": \"" + studentId + "\", \"password\": \"" + password + "\" }";

			HttpHeaders headers = new HttpHeaders();
			headers.set("accept", "application/json");
			headers.set("Content-Type", "application/json");

			HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);

			JSONObject jsonResponse = new JSONObject(response.getBody());
			JSONArray dataArray = jsonResponse.getJSONArray("data");

			List<Object> lectureList = new ArrayList<>();
			for (int i = 0; i < dataArray.length(); i++) {
				JSONObject jsonObject = dataArray.getJSONObject(i);
				lectureList.add(jsonObject.toMap());
			}
			return lectureList;
		} catch (Exception e) {
			throw new ConvertErrorException();
		}
	}
}
