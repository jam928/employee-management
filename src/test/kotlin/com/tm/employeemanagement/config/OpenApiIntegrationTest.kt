package com.tm.employeemanagement.config

import com.tm.employeemanagement.support.AbstractIntegrationTest
import org.hamcrest.Matchers.hasKey
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class OpenApiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun testApiDocsListEmployeeEndpoints() {
        this.mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.info.title").value("Employee Management API"))
            .andExpect(jsonPath("$.paths", hasKey("/employee/all")))
            .andExpect(jsonPath("$.paths", hasKey("/employee/update/{id}")))
    }

    @Test
    fun testSwaggerUiIsServed() {
        this.mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk)
    }
}
