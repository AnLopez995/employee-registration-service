package com.parameta.workforce.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI employeeApi() {
        return new OpenAPI().info(new Info()
                .title("Employee Registration API")
                .version("v1")
                .description("Registers an employee through a SOAP registry "
                        + "and returns the employee with computed age and tenure."));
    }
}