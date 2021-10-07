package com.tm.employeemanagement.service

import com.tm.employeemanagement.model.Employee
import com.tm.employeemanagement.repo.EmployeeRepo
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class EmployeeServiceTest {

    @MockK
    lateinit var employeeRepo: EmployeeRepo

    @InjectMockKs
    lateinit var employeeService: EmployeeService

    @Test
    fun testAddEmployee() {
        val employee = Employee()
        every { employeeRepo.save(employee) }.returns(employee)
        val addedEmployee = employeeService.addEmployee(employee)
        assertNotNull(addedEmployee.employeeCode)
    }

    @Test
    fun testFindAllEmployeesWithoutEmail() {
        val employee = Employee()
        val employee2 = Employee()
        val employeeList = mutableListOf<Employee>(employee, employee2)
        val pageRes: Page<Employee> = PageImpl(employeeList)
        every { employeeRepo.findAll(any<PageRequest>())}.returns(pageRes)
        val resultPage  = employeeService.findAllEmployees(10, 1, "id", null)
        assertNotNull(resultPage.content)
        assertEquals(resultPage.totalElements, 2)
    }

    @Test
    fun testFindAllEmployeesWithEmail() {
        val employee = Employee()
        val employee2 = Employee()
        val employeeList = mutableListOf<Employee>(employee, employee2)
        val pageRes: Page<Employee> = PageImpl(employeeList)
        every { employeeRepo.findByEmailContains(any(),any<PageRequest>())}.returns(pageRes)
        val resultPage  = employeeService.findAllEmployees(10, 1, "id", "email")
        assertNotNull(resultPage.content)
        assertEquals(resultPage.totalElements, 2)
    }

    @Test
    fun testUpdateEmployee() {
        val employee = Employee()
        every { employeeRepo.save(any())}.returns(employee)
        val resultEmployee  = employeeService.updateEmployee(employee)
        assertNotNull(resultEmployee)
    }

    @Test
    fun testFindEmployeeById() {
        val employee = Employee()
        every { employeeRepo.findEmployeeById(any())}.returns(employee)
        val resultEmployee  = employeeService.findEmployeeById(1)
        assertNotNull(resultEmployee)
    }
}