package sopio.acha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AchaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AchaApplication.class, args);
	}

}
