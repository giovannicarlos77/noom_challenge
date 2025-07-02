package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.constants.MessageConstants
import com.noom.interview.fullstack.sleep.dto.ApiResponse
import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<ApiResponse<User>> {
        val user = userService.createUser(request.username, request.email)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(user, MessageConstants.USER_CREATED_SUCCESSFULLY))
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<ApiResponse<User>> {
        val user = userService.getUserById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MessageConstants.RESOURCE_NOT_FOUND))
        }
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<ApiResponse<List<User>>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(ApiResponse.success(users))
    }

    @GetMapping("/by-username/{username}")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<ApiResponse<User>> {
        val user = userService.getUserByUsername(username)
        return if (user != null) {
            ResponseEntity.ok(ApiResponse.success(user))
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(MessageConstants.RESOURCE_NOT_FOUND))
        }
    }
}

