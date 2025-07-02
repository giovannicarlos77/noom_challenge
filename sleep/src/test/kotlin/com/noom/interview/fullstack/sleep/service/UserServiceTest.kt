package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateUserRequest
import com.noom.interview.fullstack.sleep.exception.ResourceConflictException
import com.noom.interview.fullstack.sleep.model.User
import com.noom.interview.fullstack.sleep.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository)
    }

    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest(
            username = "testuser",
            email = "test@example.com"
        )

        val expectedUser = User(
            id = 1L,
            username = request.username,
            email = request.email
        )

        `when`(userRepository.findByUsername(request.username)).thenReturn(null)
        `when`(userRepository.save(any<User>())).thenReturn(expectedUser)

        // When
        val result = userService.createUser(request.username, request.email)

        // Then
        assertEquals(expectedUser.id, result.id)
        assertEquals(expectedUser.username, result.username)
        assertEquals(expectedUser.email, result.email)

        verify(userRepository).findByUsername(request.username)
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `should throw exception when username already exists`() {
        // Given
        val request = CreateUserRequest(
            username = "existinguser",
            email = "test@example.com"
        )

        val existingUser = User(
            id = 1L,
            username = request.username,
            email = "existing@example.com"
        )

        `when`(userRepository.findByUsername(request.username)).thenReturn(existingUser)

        // When & Then
        val exception = assertThrows(ResourceConflictException::class.java) {
            userService.createUser(request.username, request.email)
        }

        assertTrue(exception.message!!.contains("Username '${request.username}' already exists"))
        verify(userRepository).findByUsername(request.username)
        verify(userRepository, never()).save(any<User>())
    }

    @Test
    fun `should validate user exists successfully`() {
        // Given
        val userId = 1L
        val expectedUser = User(
            id = userId,
            username = "testuser",
            email = "test@example.com"
        )

        `when`(userRepository.findById(userId)).thenReturn(expectedUser)

        // When
        val result = userService.validateUserExists(userId)

        // Then
        assertEquals(expectedUser, result)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `should throw exception when user not found`() {
        // Given
        val userId = 999L

        `when`(userRepository.findById(userId)).thenReturn(null)

        // When & Then
        val exception = assertThrows(com.noom.interview.fullstack.sleep.exception.ResourceNotFoundException::class.java) {
            userService.validateUserExists(userId)
        }

        assertTrue(exception.message!!.contains("User with id $userId not found"))
        verify(userRepository).findById(userId)
    }

    @Test
    fun `should get all users successfully`() {
        // Given
        val users = listOf(
            User(id = 1L, username = "user1", email = "user1@example.com"),
            User(id = 2L, username = "user2", email = "user2@example.com")
        )

        `when`(userRepository.findAll()).thenReturn(users)

        // When
        val result = userService.getAllUsers()

        // Then
        assertEquals(2, result.size)
        assertEquals("user1", result[0].username)
        assertEquals("user2", result[1].username)

        verify(userRepository).findAll()
    }
} 