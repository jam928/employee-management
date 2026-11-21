package com.tm.employeemanagement.advice

import com.tm.employeemanagement.service.ImageStorageException
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@ControllerAdvice
class MyControllerAdvice {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity<String>(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleEmptyResultDataAccessException(ex: EmptyResultDataAccessException): ResponseEntity<String> {
        return ResponseEntity<String>("Results not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler
    fun handleMissingServletRequestPartException(ex: MissingServletRequestPartException): ResponseEntity<String> {
        return ResponseEntity<String>("Missing image file. Send it in a multipart part named \"file\"", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): ResponseEntity<String> {
        return ResponseEntity<String>("Image is too large", HttpStatus.CONTENT_TOO_LARGE)
    }

    @ExceptionHandler
    fun handleImageStorageException(ex: ImageStorageException): ResponseEntity<String> {
        log.error(ex.message, ex)
        return ResponseEntity<String>("Image storage is unavailable", HttpStatus.SERVICE_UNAVAILABLE)
    }
}
