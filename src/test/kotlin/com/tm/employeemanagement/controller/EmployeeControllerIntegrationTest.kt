package com.tm.employeemanagement.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tm.employeemanagement.model.Employee
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
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
class EmployeeControllerIntegrationTest {
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
        val employee = objectMapper.readValue<Employee>(mvcResult.response.contentAsString)
        assertNotNull(employee)
        assertNotNull(employee.id)
    }

    @Test
    fun testGetEmployeeById() {
        val mvcResult = this.mockMvc.perform(get("/employee/find/${1}")).andReturn()
        val resultEmployee = objectMapper.readValue<Employee>(mvcResult.response.contentAsString)
        val actualEmployee = objectMapper.readValue<Employee>(File(RESOURCES_DIR+"json/add_employee.json").readText(Charsets.UTF_8))
        assertEquals(resultEmployee.name, actualEmployee.name)
    }

    @Test
    fun testUpdateEmployee() {
        // Get the original employee
        var mvcResult = this.mockMvc.perform(get("/employee/find/${1}")).andReturn()
        val originalEmployee = objectMapper.readValue<Employee>(mvcResult.response.contentAsString)

        // Update the employee name and email address
        originalEmployee.name = "John Doe"
        originalEmployee.email = "johndoe@tm.com"

        // call the update employee endpoint to save to the db
        mvcResult = this.mockMvc.perform(put("/employee/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(originalEmployee))).andReturn()

        // verify the response has the updated name and email
        val resultEmployee = objectMapper.readValue<Employee>(mvcResult.response.contentAsString)
        assertEquals(resultEmployee.name, originalEmployee.name)
        assertEquals(resultEmployee.email, originalEmployee.email)

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