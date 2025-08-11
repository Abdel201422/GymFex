package com.gymfex.usuarios_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UsuariosServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsuariosServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(KafkaTemplate<String, String> kafkaTemplate) {
		return args -> {
			
			kafkaTemplate.send("usuarios-topic", "Hello, Kafka! oooo");
		};
	}

}
