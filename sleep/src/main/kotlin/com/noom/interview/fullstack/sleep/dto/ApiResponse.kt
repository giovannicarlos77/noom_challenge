package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.constants.MessageConstants
import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, message: String = MessageConstants.OK): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data
            )
        }

        fun <T> error(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                data = data
            )
        }
    }
} 