package com.tm.employeemanagement.controller

import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import com.tm.employeemanagement.dto.EmployeeRequest
import com.tm.employeemanagement.dto.EmployeeResponse
import com.tm.employeemanagement.support.AbstractIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val RESOURCES_DIR = "src/test/resources/"

    @BeforeEach
    fun setup() {
        var json = File(RESOURCES_DIR+"json/add_employee.json").readText(Charsets.UTF_8)

        this.mockMvc.perform(post("/employee/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)).andReturn()

        json = File(RESOURCES_DIR+"json/add_employee_2.json").readText(Charsets.UTF_8)

        this.mockMvc.perform(post("/employee/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)).andReturn()
    }

    @Test
    fun testAddEmployee() {
        val json = File(RESOURCES_DIR+"json/add_employee_3.json").readText(Charsets.UTF_8)

        val mvcResult = this.mockMvc.perform(post("/employee/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)).andReturn()
        val employee = objectMapper.readValue<EmployeeResponse>(mvcResult.response.contentAsString)
        assertNotNull(employee)
        assertNotNull(employee.id)
    }

    @Test
    fun testGetEmployeeById() {
        val mvcResult = this.mockMvc.perform(get("/employee/find/${1}")).andReturn()
        val resultEmployee = objectMapper.readValue<EmployeeResponse>(mvcResult.response.contentAsString)
        val actualEmployee = objectMapper.readValue<EmployeeRequest>(File(RESOURCES_DIR+"json/add_employee.json").readText(Charsets.UTF_8))
        assertEquals(resultEmployee.name, actualEmployee.name)
    }

    @Test
    fun testGetEmployeeByNotFoundId() {
        val mvcResult = this.mockMvc.perform(get("/employee/find/${300}")).andReturn()
        assertEquals(mvcResult.response.contentAsString, "Results not found")
        assertEquals(mvcResult.response.status, 404)
    }

    @Test
    fun testUpdateEmployee() {
        // Get the original employee
        var mvcResult = this.mockMvc.perform(get("/employee/find/${1}")).andReturn()
        val originalEmployee = objectMapper.readValue<EmployeeResponse>(mvcResult.response.contentAsString)

        // Update the employee name and email address
        val updateRequest = EmployeeRequest(
            name = "John Doe",
            email = "johndoe@tm.com",
            jobTitle = originalEmployee.jobTitle,
            phone = originalEmployee.phone,
            imageUrl = originalEmployee.imageUrl
        )

        // call the update employee endpoint to save to the db
        mvcResult = this.mockMvc.perform(put("/employee/update/${1}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))).andReturn()

        // verify the response has the updated name and email, and the id and employee code are unchanged
        val resultEmployee = objectMapper.readValue<EmployeeResponse>(mvcResult.response.contentAsString)
        assertEquals(resultEmployee.name, updateRequest.name)
        assertEquals(resultEmployee.email, updateRequest.email)
        assertEquals(resultEmployee.id, originalEmployee.id)
        assertEquals(resultEmployee.employeeCode, originalEmployee.employeeCode)
    }

    @Test
    fun testUpdateEmployeeNotFound() {
        val json = File(RESOURCES_DIR+"json/add_employee_3.json").readText(Charsets.UTF_8)
        val mvcResult = this.mockMvc.perform(put("/employee/update/${300}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)).andReturn()
        assertEquals(mvcResult.response.contentAsString, "Results not found")
        assertEquals(mvcResult.response.status, 404)
    }

    @Test
    fun testGetAllEmployees() {
        val pageSize = 10
        val pageNumber = 0
        this.mockMvc.perform(get("/employee/all?pageSize=${pageSize}&pageNumber=${pageNumber}")).andExpect(status().isOk())
    }

    @Test
    fun testDeleteEmployee() {
        this.mockMvc.perform(delete("/employee/delete/${2}")).andExpect(status().isOk()).andExpect(status().is2xxSuccessful)
    }

}