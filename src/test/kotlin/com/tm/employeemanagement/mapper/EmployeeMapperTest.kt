package com.tm.employeemanagement.mapper

import com.tm.employeemanagement.dto.EmployeeRequest
import com.tm.employeemanagement.dto.EmployeeResponse
import com.tm.employeemanagement.model.Employee
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmployeeMapperTest {

    private val employeeMapper: EmployeeMapper = EmployeeMapperImpl()

    private val request = EmployeeRequest("Joel Lan", "joe@tm.com", "Java", "1234567890",
        "https://bootdey.com/img/Content/avatar/avatar3.png")

    @Test
    fun testToResponse() {
        val employee = Employee().apply {
            id = 1
            name = "Dave Rogers"
            email = "dave@tm.com"
            jobTitle = "Python"
            phone = "4325678900"
            imageUrl = "https://bootdey.com/img/Content/avatar/avatar2.png"
            employeeCode = "a22294f7-6c5b-4498-ab4f-e412fa226756"
        }

        val response = employeeMapper.toResponse(employee)

        assertEquals(EmployeeResponse(1, "Dave Rogers", "dave@tm.com", "Python", "4325678900",
            "https://bootdey.com/img/Content/avatar/avatar2.png", "a22294f7-6c5b-4498-ab4f-e412fa226756"), response)
    }

    @Test
    fun testToEntity() {
        val employee = employeeMapper.toEntity(request)

        assertNull(employee.id)
        assertNull(employee.employeeCode)
        assertEquals(request.name, employee.name)
        assertEquals(request.email, employee.email)
        assertEquals(request.jobTitle, employee.jobTitle)
        assertEquals(request.phone, employee.phone)
        assertEquals(request.imageUrl, employee.imageUrl)
    }

    @Test
    fun testUpdateEntityKeepsIdAndEmployeeCode() {
        val employee = Employee().apply {
            id = 2
            name = "Old Name"
            employeeCode = "610d7ef6-8fc0-4eb5-ab30-838982558b65"
        }

        employeeMapper.updateEntity(request, employee)

        assertEquals(2, employee.id)
        assertEquals("610d7ef6-8fc0-4eb5-ab30-838982558b65", employee.employeeCode)
        assertEquals(request.name, employee.name)
        assertEquals(request.email, employee.email)
        assertEquals(request.jobTitle, employee.jobTitle)
    }
}
