package sopio.acha.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

	@Value("${firebase.key-path}")
	private String FirebaseAccountKeyPath;

	@Bean
	FirebaseApp firebaseApp() throws IOException {
		FileInputStream serviceAccount = new FileInputStream(FirebaseAccountKeyPath);
		FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();
		return FirebaseApp.initializeApp(options);
	}
}
