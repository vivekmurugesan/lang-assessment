package com.langassessment;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class LangAssessmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(LangAssessmentApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofSeconds(20))
				.setReadTimeout(Duration.ofSeconds(120))
				.build();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Language Assessment API")
						.version("1.0.0")
						.description("API for Language Assessment System - CEFR based evaluation")
						.contact(new Contact()
								.name("Language Assessment Team")
								.email("support@langassessment.com"))
						.license(new License()
								.name("MIT License")));
	}
}
