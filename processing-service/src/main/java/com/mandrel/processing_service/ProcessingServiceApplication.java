package com.mandrel.processing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.processing_service.processing_service.client")
public class ProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcessingServiceApplication.class, args);
	}
}
