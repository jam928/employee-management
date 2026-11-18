package com.tm.employeemanagement.dto

/**
 * Body for creating or updating an employee. The id and employeeCode are
 * assigned by the server, so clients can't set them.
 */
data class EmployeeRequest(
    var name: String? = null,
    var email: String? = null,
    var jobTitle: String? = null,
    var phone: String? = null,
    var imageUrl: String? = null
)
