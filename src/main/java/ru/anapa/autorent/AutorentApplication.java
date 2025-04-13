package ru.anapa.autorent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutorentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutorentApplication.class, args);
	}

}
