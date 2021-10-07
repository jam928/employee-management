package com.tm.employeemanagement.utility

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Component
class CorsUtility {
    @Bean
    fun corsFilter() : CorsFilter {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowCredentials = true
        corsConfiguration.allowedOrigins = mutableListOf("http://localhost:4200")
        corsConfiguration.allowedHeaders = mutableListOf("Origin", "Access-Control-Allow-Origin", "Content-Type",
            "Accept", "Authorization", "Origin, Accept", "X-Requested-With",
            "Access-Control-Request-Method", "Accees-Control-Request-Headers")
        corsConfiguration.exposedHeaders = mutableListOf("Origin", "Content-Type", "Content-Type", "Accept", "Authorization",
            "Access-Control-Allow-Origin","Access-Control-Allow-Origin","Access-Control-Allow-Credentials")
        corsConfiguration.allowedMethods = mutableListOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        val urlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)
        return CorsFilter(urlBasedCorsConfigurationSource)
    }

}