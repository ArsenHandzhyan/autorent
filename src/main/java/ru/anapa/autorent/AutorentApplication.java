package ru.anapa.autorent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "ru.anapa.autorent.config",
        "ru.anapa.autorent.util",
        "ru.anapa.autorent.repository",
        "ru.anapa.autorent.service",
        "ru.anapa.autorent.controller"
})
public class AutorentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutorentApplication.class, args);
	}

}
