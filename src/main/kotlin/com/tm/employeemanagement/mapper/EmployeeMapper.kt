package com.tm.employeemanagement.mapper

import com.tm.employeemanagement.dto.EmployeeRequest
import com.tm.employeemanagement.dto.EmployeeResponse
import com.tm.employeemanagement.model.Employee
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Mappings

@Mapper
interface EmployeeMapper {
    fun toResponse(employee: Employee): EmployeeResponse

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "employeeCode", ignore = true)
    )
    fun toEntity(employeeRequest: EmployeeRequest): Employee

    /** Copies the request's fields onto an existing employee, leaving id and employeeCode untouched. */
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "employeeCode", ignore = true)
    )
    fun updateEntity(employeeRequest: EmployeeRequest, @MappingTarget employee: Employee)
}
