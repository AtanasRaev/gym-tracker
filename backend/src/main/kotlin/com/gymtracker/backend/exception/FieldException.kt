package com.gymtracker.backend.exception

import org.springframework.http.HttpStatus

/** An error that belongs to one request field, so that the client can show it next to that field. */
class FieldException(
    val status: HttpStatus,
    val field: String,
    override val message: String
) : RuntimeException(message)
