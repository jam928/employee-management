package com.tm.employeemanagement.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun employeeManagementOpenApi(): OpenAPI = OpenAPI()
        .info(Info()
            .title("Employee Management API")
            .description("Create, find, update and delete employees")
            .version("v1"))
}
