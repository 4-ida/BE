package com.pillmate.pillmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PillmateApplication {

	public static void main(String[] args) {
		SpringApplication.run(PillmateApplication.class, args);
	}

}
