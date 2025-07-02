package com.noom.interview.fullstack.sleep.model

import java.time.OffsetDateTime

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

