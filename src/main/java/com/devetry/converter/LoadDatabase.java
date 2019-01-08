package com.devetry.converter;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class loadDatabase {
	@Bean
	CommandLineRunner initDatabase(ConverterRepository repository) {
		return args -> {
			System.out.println("db probably initialized?");
		};
	}
}