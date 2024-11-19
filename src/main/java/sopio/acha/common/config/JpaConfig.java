package sopio.acha.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "sopio.acha")
@EnableJpaRepositories(basePackages = "sopio.acha")
public class JpaConfig {

}

