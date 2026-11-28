package com.gymtracker.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

/**
 * Returns errors as RFC 9457 problem details: `detail` is a message for the user,
 * and `fieldErrors` (when present) maps a request field to its message.
 */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(exception: MethodArgumentNotValidException): ProblemDetail {
        // One message per field: the first one that failed
        val fieldErrors = exception.bindingResult.fieldErrors
            .groupBy { it.field }
            .mapValues { (_, errors) -> errors.first().defaultMessage ?: "Invalid value" }

        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Check the highlighted fields.").apply {
            setProperty("fieldErrors", fieldErrors)
        }
    }

    @ExceptionHandler(FieldException::class)
    fun handleField(exception: FieldException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(exception.status, exception.message).apply {
            setProperty("fieldErrors", mapOf(exception.field to exception.message))
        }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleStatus(exception: ResponseStatusException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            exception.statusCode,
            exception.reason ?: "The request failed."
        )
}
