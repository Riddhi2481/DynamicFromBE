package com.example.praticalTestBE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PraticalTestBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraticalTestBeApplication.class, args);
	}

}
