package com.tm.employeemanagement.dto

data class EmployeeResponse(
    var id: Long? = null,
    var name: String? = null,
    var email: String? = null,
    var jobTitle: String? = null,
    var phone: String? = null,
    var imageUrl: String? = null,
    var employeeCode: String? = null
)
