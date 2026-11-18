package com.tm.employeemanagement.controller

import com.tm.employeemanagement.dto.EmployeeRequest
import com.tm.employeemanagement.dto.EmployeeResponse
import com.tm.employeemanagement.mapper.EmployeeMapper
import com.tm.employeemanagement.service.EmployeeService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employee")
class EmployeeController(val employeeService: EmployeeService, val employeeMapper: EmployeeMapper) {

    @GetMapping("/all")
    fun getAllEmployees(@RequestParam(defaultValue = "10") pageSize: Int,
                        @RequestParam(defaultValue = "0") pageNumber: Int,
                        @RequestParam(defaultValue = "id") sortBy: String,
                        @RequestParam(required = false) email: String?): ResponseEntity<Page<EmployeeResponse>> {
        val employees = employeeService.findAllEmployees(pageNumber, pageSize, sortBy, email).map(employeeMapper::toResponse)
        return ResponseEntity(employees, HttpStatus.OK)
    }

    @GetMapping("/find/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long) : ResponseEntity<EmployeeResponse> {
        val employee = employeeMapper.toResponse(employeeService.findEmployeeById(id))
        return ResponseEntity(employee, HttpStatus.OK)
    }

    @PostMapping("/add")
    fun addEmployee(@RequestBody employeeRequest: EmployeeRequest) : ResponseEntity<EmployeeResponse> {
        val newEmployee = employeeMapper.toResponse(employeeService.addEmployee(employeeMapper.toEntity(employeeRequest)))
        return ResponseEntity(newEmployee, HttpStatus.CREATED)
    }

    @PutMapping("/update/{id}")
    fun updateEmployee(@PathVariable("id") id: Long,
                       @RequestBody employeeRequest: EmployeeRequest) : ResponseEntity<EmployeeResponse> {
        val employee = employeeService.findEmployeeById(id)
        employeeMapper.updateEntity(employeeRequest, employee)
        val newEmployee = employeeMapper.toResponse(employeeService.updateEmployee(employee))
        return ResponseEntity(newEmployee, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteEmployee(@PathVariable("id") id: Long) : ResponseEntity<String> {
        val deletedEmployee = employeeService.deleteEmployee(id)
        return ResponseEntity(deletedEmployee.toString(),HttpStatus.OK)
    }
}