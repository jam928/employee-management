package com.tm.employeemanagement.advice

import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class MyControllerAdvice {

    @ExceptionHandler
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity<String>(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleEmptyResultDataAccessException(ex: EmptyResultDataAccessException): ResponseEntity<String> {
        return ResponseEntity<String>("Results not found", HttpStatus.NOT_FOUND)
    }
}