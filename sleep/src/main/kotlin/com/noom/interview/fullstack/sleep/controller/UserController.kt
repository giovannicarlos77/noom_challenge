package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class CreateUserRequest(
    val username: String,
    val email: String
)

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = ["*"])
class UserController(private val userRepository: UserRepository) {

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<User> {
        return try {
            // Check if username already exists
            userRepository.findByUsername(request.username)?.let {
                return ResponseEntity.status(HttpStatus.CONFLICT).build()
            }

            val user = User(
                username = request.username,
                email = request.email
            )
            val savedUser = userRepository.save(user)
            ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        val user = userRepository.findById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        val users = userRepository.findAll()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/by-username/{username}")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<User> {
        val user = userRepository.findByUsername(username)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

