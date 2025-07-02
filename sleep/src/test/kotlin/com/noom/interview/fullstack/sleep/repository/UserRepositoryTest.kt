package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("unittest")
@Transactional
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should save and find user by id`() {
        // Given
        val user = User(
            username = "testuser",
            email = "test@example.com"
        )

        // When
        val savedUser = userRepository.save(user)

        // Then
        assertNotNull(savedUser.id)
        assertEquals("testuser", savedUser.username)
        assertEquals("test@example.com", savedUser.email)
        assertNotNull(savedUser.createdAt)
        assertNotNull(savedUser.updatedAt)

        // Verify we can find it by ID
        val foundUser = userRepository.findById(savedUser.id!!)
        assertNotNull(foundUser)
        assertEquals(savedUser.username, foundUser!!.username)
        assertEquals(savedUser.email, foundUser.email)
    }

    @Test
    fun `should find user by username`() {
        // Given
        val user = User(
            username = "uniqueuser",
            email = "unique@example.com"
        )
        val savedUser = userRepository.save(user)

        // When
        val foundUser = userRepository.findByUsername("uniqueuser")

        // Then
        assertNotNull(foundUser)
        assertEquals(savedUser.id, foundUser!!.id)
        assertEquals("uniqueuser", foundUser.username)
        assertEquals("unique@example.com", foundUser.email)
    }

    @Test
    fun `should return null when user not found by id`() {
        // When
        val foundUser = userRepository.findById(999999L)

        // Then
        assertNull(foundUser)
    }

    @Test
    fun `should return null when user not found by username`() {
        // When
        val foundUser = userRepository.findByUsername("nonexistentuser")

        // Then
        assertNull(foundUser)
    }

    @Test
    fun `should find all users`() {
        // Given
        val user1 = User(username = "user1", email = "user1@example.com")
        val user2 = User(username = "user2", email = "user2@example.com")
        
        userRepository.save(user1)
        userRepository.save(user2)

        // When
        val allUsers = userRepository.findAll()

        // Then
        assertTrue(allUsers.size >= 2)
        assertTrue(allUsers.any { it.username == "user1" })
        assertTrue(allUsers.any { it.username == "user2" })
    }
}

