package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.constants.MessageConstants
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class CreateUserRequest(
    @field:NotBlank(message = MessageConstants.USERNAME_REQUIRED)
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,
    
    @field:NotBlank(message = MessageConstants.EMAIL_REQUIRED)
    @field:Email(message = MessageConstants.EMAIL_INVALID_FORMAT)
    val email: String
) 