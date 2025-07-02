package com.noom.interview.fullstack.sleep.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ResourceConflictException(message: String) : RuntimeException(message) 