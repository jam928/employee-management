package com.tm.employeemanagement.repo

import com.tm.employeemanagement.model.Employee
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeRepo: JpaRepository<Employee, Long> {
    fun deleteEmployeeById(id: Long) : Int
    fun findEmployeeById(id: Long): Employee
    fun findByEmailContains(email: String, pageable: Pageable): Page<Employee>
}