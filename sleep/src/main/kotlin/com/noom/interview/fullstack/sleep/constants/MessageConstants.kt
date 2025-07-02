package com.noom.interview.fullstack.sleep.constants

object MessageConstants {
    // Error Messages
    const val USER_NOT_FOUND = "User with id %d not found"
    const val USERNAME_ALREADY_EXISTS = "Username '%s' already exists"
    const val SLEEP_LOG_ALREADY_EXISTS = "Sleep log already exists for date %s"
    const val INVALID_REQUEST = "Invalid request"
    const val RESOURCE_NOT_FOUND = "Resource not found"
    
    // Success Messages
    const val USER_CREATED_SUCCESSFULLY = "User created successfully"
    const val SLEEP_LOG_CREATED_SUCCESSFULLY = "Sleep log created successfully"
    
    // Validation Messages
    const val USERNAME_REQUIRED = "Username is required"
    const val EMAIL_REQUIRED = "Email is required"
    const val EMAIL_INVALID_FORMAT = "Email format is invalid"
    const val SLEEP_DATE_REQUIRED = "Sleep date is required"
    const val BEDTIME_REQUIRED = "Bedtime is required"
    const val WAKE_TIME_REQUIRED = "Wake time is required"
    const val MORNING_FEELING_REQUIRED = "Morning feeling is required"
    
    // API Response Messages
    const val CREATED = "Created"
    const val OK = "OK"
    const val BAD_REQUEST = "Bad Request"
    const val NOT_FOUND = "Not Found"
    const val CONFLICT = "Conflict"
    const val INTERNAL_SERVER_ERROR = "Internal Server Error"
} 