package sopio.acha.common.handler;

import static org.springframework.http.HttpMethod.POST;
import static sopio.acha.common.handler.DateHandler.getCurrentSemester;
import static sopio.acha.common.handler.DateHandler.getCurrentSemesterYear;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.exception.ExtractorErrorException;

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
			URI uri = buildUriByPath("/v1/timetable/");
			String requestBody = "{ \"studentId\": \"" + studentId + "\", \"password\": \"" + password
				+ "\", \"year\": " + getCurrentSemesterYear() + ", \"semester\": " + getCurrentSemester()
				+ ", \"extract\": true }";
			JSONObject jsonObject = getJsonData(requestBody, uri);
			JSONArray dataArray = jsonObject.getJSONArray("data");
			List<Object> lectureList = new ArrayList<>();
			for (int i = 0; i < dataArray.length(); i++) {
				lectureList.add(dataArray.getJSONObject(i).toMap());
			}
			return lectureList;
		} catch (Exception e) {
			throw new ExtractorErrorException();
		}
	}

	public static void requestAuthentication(String studentId, String password) {
		URI uri = buildUriByPath("/v1/auth/");
		String requestBody =
			"{ \"studentId\": \"" + studentId + "\", \"password\": \"" + password + "\", \"extract\": false }";
		JSONObject jsonObject = getJsonData(requestBody, uri);
		if (!jsonObject.get("verification").toString().equals("true")) {
			throw new ExtractorErrorException();
		}
	}

	public static String requestAuthenticationAndUserInfo(String studentId, String password) {
		URI uri = buildUriByPath("/v1/auth/");
		String requestBody =
			"{ \"studentId\": \"" + studentId + "\", \"password\": \"" + password + "\", \"extract\": true }";
		return getJsonData(requestBody, uri).toString();
	}

	public static String requestCourse(String studentId, String password) {
		URI uri = buildUriByPath("/v1/course/");
		String requestBody = "{ \"studentId\": \"" + studentId + "\", \"password\": \"" + password
			+ "\", \"year\": " + getCurrentSemesterYear() + ", \"semester\": " + getCurrentSemester()
			+ ", \"extract\": true }";
		return getJsonData(requestBody, uri).toString();
	}

	private static URI buildUriByPath(String path) {
		return UriComponentsBuilder
			.fromUriString(requestUrl)
			.path(path)
			.encode()
			.build()
			.toUri();
	}

	private static JSONObject getJsonData(String requestBody, URI uri) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("accept", "application/json");
		headers.set("Content-Type", "application/json");

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(uri, POST, entity, String.class);
		return new JSONObject(response.getBody());
	}
}
