package com.tm.employeemanagement.utility

import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component


@Component
class ActuatorUtility {
    @Bean
    fun htttpTraceRepository(): HttpTraceRepository? {
        return InMemoryHttpTraceRepository()
    }
}