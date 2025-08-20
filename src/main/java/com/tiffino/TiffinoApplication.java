package com.tiffino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TiffinoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiffinoApplication.class, args);
	}
}
