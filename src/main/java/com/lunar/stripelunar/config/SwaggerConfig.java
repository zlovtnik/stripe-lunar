package com.lunar.stripelunar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stripe-Lunar ETL API")
                        .description("API for Stripe data ETL operations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Lunar Team")
                                .url("https://lunar.com")
                                .email("support@lunar.com")));
    }
}
