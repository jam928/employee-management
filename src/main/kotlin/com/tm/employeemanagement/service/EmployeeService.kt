package com.tm.employeemanagement.service

import com.tm.employeemanagement.model.Employee
import com.tm.employeemanagement.repo.EmployeeRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class EmployeeService(private val employeeRepo: EmployeeRepo) {

    fun addEmployee(employee: Employee): Employee {
        employee.employeeCode = UUID.randomUUID().toString()
        return employeeRepo.save(employee)
    }

    fun findAllEmployees(pageNumber: Int, pageSize: Int, sortBy: String, email: String?): Page<Employee> {
        val paging = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy))
        if(email != null) {
            return employeeRepo.findByEmailContains(email, paging)
        }
        return employeeRepo.findAll(paging)
    }

    fun updateEmployee(employee: Employee): Employee {
        return employeeRepo.save(employee)
    }

    fun findEmployeeById(id: Long): Employee {
        return employeeRepo.findEmployeeById(id)
    }

    fun deleteEmployee(id: Long) : Int {
        return employeeRepo.deleteEmployeeById(id)
    }
}