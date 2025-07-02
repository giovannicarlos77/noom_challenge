package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.constants.MessageConstants
import com.noom.interview.fullstack.sleep.exception.ResourceConflictException
import com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(username: String, email: String): User {
        // Check if username already exists
        userRepository.findByUsername(username)?.let {
            throw ResourceConflictException(MessageConstants.USERNAME_ALREADY_EXISTS.format(username))
        }

        val user = User(
            username = username,
            email = email
        )
        
        return userRepository.save(user)
    }

    fun getUserById(id: Long): User? {
        return userRepository.findById(id)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun validateUserExists(userId: Long): User {
        return userRepository.findById(userId) 
            ?: throw ResourceNotFoundException(MessageConstants.USER_NOT_FOUND.format(userId))
    }
}