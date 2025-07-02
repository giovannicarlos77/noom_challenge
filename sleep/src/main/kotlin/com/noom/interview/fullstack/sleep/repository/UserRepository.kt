package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.User
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.time.OffsetDateTime

@Repository
class UserRepository(private val jdbcTemplate: JdbcTemplate) {

    private val userRowMapper = RowMapper<User> { rs, _ ->
        User(
            id = rs.getLong("id"),
            username = rs.getString("username"),
            email = rs.getString("email"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    fun findById(id: Long): User? {
        return try {
            jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                userRowMapper,
                id
            )
        } catch (e: Exception) {
            null
        }
    }

    fun findByUsername(username: String): User? {
        return try {
            jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE username = ?",
                userRowMapper,
                username
            )
        } catch (e: Exception) {
            null
        }
    }

    fun save(user: User): User {
        val keyHolder = GeneratedKeyHolder()
        
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(
                "INSERT INTO users (username, email) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setString(1, user.username)
            ps.setString(2, user.email)
            ps
        }, keyHolder)

        val generatedId = keyHolder.key?.toLong() ?: throw RuntimeException("Failed to get generated ID")
        return findById(generatedId)!!
    }

    fun findAll(): List<User> {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", userRowMapper)
    }
}

