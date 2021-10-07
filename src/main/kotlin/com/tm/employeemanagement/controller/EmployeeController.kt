package com.tm.employeemanagement.controller

import com.tm.employeemanagement.model.Employee
import com.tm.employeemanagement.service.EmployeeService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employee")
class EmployeeController(val employeeService: EmployeeService) {

    @GetMapping("/all")
    fun getAllEmployees(@RequestParam(defaultValue = "10") pageSize: Int,
                        @RequestParam(defaultValue = "0") pageNumber: Int,
                        @RequestParam(defaultValue = "id") sortBy: String,
                        @RequestParam(required = false) email: String?): ResponseEntity<Page<Employee>> {
        val employees = employeeService.findAllEmployees(pageNumber, pageSize, sortBy, email)
        return ResponseEntity(employees, HttpStatus.OK)
    }

    @GetMapping("/find/{id}")
    fun getEmployeeById(@PathVariable("id") id: Long) : ResponseEntity<Employee> {
        val employee = employeeService.findEmployeeById(id)
        return ResponseEntity(employee, HttpStatus.OK)
    }

    @PostMapping("/add")
    fun addEmployee(@RequestBody employee: Employee) : ResponseEntity<Employee> {
        val newEmployee = employeeService.addEmployee(employee)
        return ResponseEntity(newEmployee, HttpStatus.CREATED)
    }

    @PutMapping("/update")
    fun updateEmployee(@RequestBody employee: Employee) : ResponseEntity<Employee> {
        val newEmployee = employeeService.updateEmployee(employee)
        return ResponseEntity(newEmployee, HttpStatus.OK)
    }

    @DeleteMapping("/delete/{id}")
    fun deleteEmployee(@PathVariable("id") id: Long) : ResponseEntity<Any> {
        employeeService.deleteEmployee(id)
        return ResponseEntity(HttpStatus.OK)
    }
}