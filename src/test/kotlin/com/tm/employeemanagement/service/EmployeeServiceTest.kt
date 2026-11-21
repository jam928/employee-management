package com.tm.employeemanagement.service

import com.tm.employeemanagement.model.Employee
import com.tm.employeemanagement.repo.EmployeeRepo
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.mock.web.MockMultipartFile
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
class EmployeeServiceTest {

    @MockK
    lateinit var employeeRepo: EmployeeRepo

    @MockK
    lateinit var imageStorageService: ImageStorageService

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

    @Test
    fun testUpdateEmployeeImageReplacesAndDeletesPreviousImage() {
        val employee = Employee().apply { id = 1; imageUrl = "http://localhost:9000/employee-images/old.png" }
        val file = MockMultipartFile("file", "new.png", "image/png", byteArrayOf(1))
        every { employeeRepo.findEmployeeById(1) }.returns(employee)
        every { imageStorageService.uploadEmployeeImage(1, file) }.returns("http://localhost:9000/employee-images/new.png")
        every { employeeRepo.save(employee) }.returns(employee)
        every { imageStorageService.deleteImage(any()) } just runs

        val result = employeeService.updateEmployeeImage(1, file)

        assertEquals("http://localhost:9000/employee-images/new.png", result.imageUrl)
        verify { imageStorageService.deleteImage("http://localhost:9000/employee-images/old.png") }
    }

    @Test
    fun testUpdateEmployeeImageRemovesUploadWhenSaveFails() {
        val employee = Employee().apply { id = 1; imageUrl = "http://localhost:9000/employee-images/old.png" }
        val file = MockMultipartFile("file", "new.png", "image/png", byteArrayOf(1))
        every { employeeRepo.findEmployeeById(1) }.returns(employee)
        every { imageStorageService.uploadEmployeeImage(1, file) }.returns("http://localhost:9000/employee-images/new.png")
        every { employeeRepo.save(employee) }.throws(RuntimeException("db down"))
        every { imageStorageService.deleteImage(any()) } just runs

        assertFailsWith<RuntimeException> { employeeService.updateEmployeeImage(1, file) }

        verify { imageStorageService.deleteImage("http://localhost:9000/employee-images/new.png") }
        verify(exactly = 0) { imageStorageService.deleteImage("http://localhost:9000/employee-images/old.png") }
    }

    @Test
    fun testRemoveEmployeeImage() {
        val employee = Employee().apply { id = 1; imageUrl = "http://localhost:9000/employee-images/old.png" }
        every { employeeRepo.findEmployeeById(1) }.returns(employee)
        every { employeeRepo.save(employee) }.returns(employee)
        every { imageStorageService.deleteImage(any()) } just runs

        val result = employeeService.removeEmployeeImage(1)

        assertNull(result.imageUrl)
        verify { imageStorageService.deleteImage("http://localhost:9000/employee-images/old.png") }
    }

    @Test
    fun testDeleteEmployeeAlsoDeletesImage() {
        val employee = Employee().apply { id = 1; imageUrl = "http://localhost:9000/employee-images/old.png" }
        every { employeeRepo.findById(1) }.returns(Optional.of(employee))
        every { employeeRepo.deleteEmployeeById(1) }.returns(1)
        every { imageStorageService.deleteImage(any()) } just runs

        assertEquals(1, employeeService.deleteEmployee(1))
        verify { imageStorageService.deleteImage("http://localhost:9000/employee-images/old.png") }
    }
}