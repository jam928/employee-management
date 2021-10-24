package com.tm.employeemanagement.repo

import org.springframework.boot.actuate.trace.http.HttpTrace
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicReference

@Repository
class CustomTraceRepository() : InMemoryHttpTraceRepository() {
    var lastTrace = AtomicReference<HttpTrace>()
    final val limit = 1000
    init {
        super.setCapacity(limit)
    }

    override fun add(map: HttpTrace) {
        super.add(map)
    }
}