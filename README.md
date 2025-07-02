# Sleep Logger API Refactoring Summary

## Overview
This document summarizes the refactoring improvements made to the Sleep Logger API to follow best practices and improve code organization.

## Key Improvements Made

### 1. **Separation of Concerns**
- **Moved business logic from controllers to services**
  - Created `UserService` to handle user-related operations
  - Enhanced `SleepLogService` with better error handling
  - Controllers now only handle HTTP requests/responses

### 2. **Constants Management**
- **Created `MessageConstants.kt`** with centralized message fields:
  - Error messages
  - Success messages  
  - Validation messages
  - API response messages
- **Benefits**: Consistent messaging, easy maintenance, no hardcoded strings

### 3. **Standardized API Responses**
- **Created `ApiResponse<T>` wrapper** for consistent response structure:
  ```kotlin
  data class ApiResponse<T>(
      val success: Boolean,
      val message: String,
      val data: T? = null,
      val timestamp: LocalDateTime = LocalDateTime.now()
  )
  ```
- **Benefits**: Consistent API responses, better error handling, improved client experience

### 4. **Enhanced Error Handling**
- **Global Exception Handler** (`GlobalExceptionHandler.kt`):
  - Centralized error handling
  - Consistent error responses
  - Proper HTTP status codes
- **Custom Exceptions**:
  - `ResourceNotFoundException` for 404 errors
  - `ResourceConflictException` for 409 conflicts
- **Benefits**: Better error messages, proper HTTP status codes, cleaner code

### 5. **Improved Validation**
- **Enhanced DTOs with validation annotations**:
  - `CreateUserRequest` with username/email validation
  - `CreateSleepLogRequest` with date/time validation
- **Benefits**: Input validation, better error messages, data integrity

### 6. **Code Organization**
- **Moved DTOs to separate files**:
  - `CreateUserRequest.kt`
  - `CreateSleepLogRequest.kt` (enhanced)
- **Benefits**: Better organization, reusability, maintainability

## File Structure Changes

### New Files Created:
```
src/main/kotlin/com/noom/interview/fullstack/sleep/
├── constants/
│   └── MessageConstants.kt
├── dto/
│   ├── ApiResponse.kt
│   └── CreateUserRequest.kt
├── exception/
│   ├── GlobalExceptionHandler.kt
│   ├── ResourceNotFoundException.kt
│   └── ResourceConflictException.kt
└── service/
    └── UserService.kt
```

### Modified Files:
- `SleepLogController.kt` - Removed business logic, added ApiResponse wrapper
- `UserController.kt` - Removed business logic, added ApiResponse wrapper
- `SleepLogService.kt` - Enhanced with better error handling
- `CreateSleepLogRequest.kt` - Added validation annotations

## Benefits Achieved

### 1. **Maintainability**
- Clear separation of concerns
- Centralized constants and messages
- Consistent error handling

### 2. **Testability**
- Business logic isolated in services
- Easier to unit test individual components
- Mock dependencies effectively

### 3. **Scalability**
- Modular architecture
- Easy to add new features
- Consistent patterns across the codebase

### 4. **User Experience**
- Consistent API responses
- Better error messages
- Proper HTTP status codes

### 5. **Code Quality**
- No hardcoded strings
- Proper validation
- Clean, readable code

## Best Practices Implemented

1. **Single Responsibility Principle**: Each class has a single, well-defined purpose
2. **Dependency Injection**: Services are properly injected into controllers
3. **Exception Handling**: Centralized, consistent error handling
4. **Validation**: Input validation at the DTO level
5. **Constants Management**: No hardcoded strings in the codebase
6. **API Design**: Consistent response structure across all endpoints

## Usage Examples

### Before (Controller with business logic):
```kotlin
@PostMapping
fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<User> {
    return try {
        // Check if username already exists
        userRepository.findByUsername(request.username)?.let {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
        val user = User(username = request.username, email = request.email)
        val savedUser = userRepository.save(user)
        ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
    } catch (e: Exception) {
        ResponseEntity.badRequest().build()
    }
}
```

### After (Clean controller with service):
```kotlin
@PostMapping
fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<ApiResponse<User>> {
    val user = userService.createUser(request.username, request.email)
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success(user, MessageConstants.USER_CREATED_SUCCESSFULLY))
}
```

This refactoring significantly improves the codebase's maintainability, testability, and adherence to best practices while providing a better developer and user experience.

## Getting Started with Docker

### Prerequisites
- Docker
- Docker Compose
- Ports 5432 (PostgreSQL) and 8080 (API) must be available

### Quick Start with Docker Compose

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd BackendTakeHomeTest
   ```

2. **Start the application**
   ```bash
   docker-compose up
   ```
   
   Or run in detached mode:
   ```bash
   docker-compose up -d
   ```

3. **Build and start (if you've made changes)**
   ```bash
   docker-compose up --build
   ```

4. **Stop the application**
   ```bash
   docker-compose down
   ```

### Docker Services

The application consists of two services:

- **PostgreSQL Database** (port 5432)
  - Database: `postgres`
  - Username: `user`
  - Password: `password`

- **Sleep Logger API** (port 8080)
  - Spring Boot application
  - Connects to PostgreSQL database
  - REST API endpoints available

### API Endpoints

Once the application is running, you can access the API at:
- **Base URL**: `http://localhost:8080`
- **API Documentation**: Available through the endpoints below

#### User Management
- `POST /api/users` - Create a new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users
- `GET /api/users/by-username/{username}` - Get user by username

#### Sleep Log Management
- `POST /api/users/{userId}/sleep` - Create sleep log
- `GET /api/users/{userId}/sleep/last-night` - Get last night's sleep
- `GET /api/users/{userId}/sleep/statistics/30-days` - Get 30-day statistics

### Testing the API

You can test the API using the provided test script:
```bash
./test_api.sh
```

Or use the Postman collection:
- Import `Sleep_Logger_API.postman_collection.json` into Postman

### Troubleshooting

1. **Port conflicts**: Ensure ports 5432 and 8080 are not in use
2. **Database connection**: Check if PostgreSQL container is running
3. **API not responding**: Verify the Spring Boot application started successfully

### Development

For development, you can:
- Mount the source code as a volume for live reloading
- Use the existing Docker setup for consistent environments
- Run tests using the provided test suite 