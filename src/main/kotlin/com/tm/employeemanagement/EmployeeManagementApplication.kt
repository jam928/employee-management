package com.tm.employeemanagement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
class EmployeeManagementApplication

fun main(args: Array<String>) {
	runApplication<EmployeeManagementApplication>(*args)
}

