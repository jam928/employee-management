package com.tm.employeemanagement.repo

import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.stereotype.Component

/** Keeps the last 1000 HTTP requests for the /actuator/httpexchanges endpoint. */
@Component
class CustomTraceRepository : InMemoryHttpExchangeRepository() {
    companion object {
        const val LIMIT = 1000
    }

    init {
        setCapacity(LIMIT)
    }
}
