package com.tm.employeemanagement.service

import com.tm.employeemanagement.model.Employee
import com.tm.employeemanagement.repo.EmployeeRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
@Transactional
class EmployeeService(private val employeeRepo: EmployeeRepo, private val imageStorageService: ImageStorageService) {

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

    /** Uploads a new image for the employee, points imageUrl at it, and removes the image it replaces. */
    fun updateEmployeeImage(id: Long, file: MultipartFile): Employee {
        val employee = employeeRepo.findEmployeeById(id)
        val previousImageUrl = employee.imageUrl
        val newImageUrl = imageStorageService.uploadEmployeeImage(id, file)
        employee.imageUrl = newImageUrl
        val saved = try {
            employeeRepo.save(employee)
        } catch (ex: Exception) {
            // Don't leave an orphaned upload behind if the employee couldn't be saved
            imageStorageService.deleteImage(newImageUrl)
            throw ex
        }
        imageStorageService.deleteImage(previousImageUrl)
        return saved
    }

    /** Clears the employee's image and removes the stored file. */
    fun removeEmployeeImage(id: Long): Employee {
        val employee = employeeRepo.findEmployeeById(id)
        val previousImageUrl = employee.imageUrl
        employee.imageUrl = null
        val saved = employeeRepo.save(employee)
        imageStorageService.deleteImage(previousImageUrl)
        return saved
    }

    fun deleteEmployee(id: Long) : Int {
        val imageUrl = employeeRepo.findById(id).map { it.imageUrl }.orElse(null)
        val deleted = employeeRepo.deleteEmployeeById(id)
        if (deleted > 0) {
            imageStorageService.deleteImage(imageUrl)
        }
        return deleted
    }
}
