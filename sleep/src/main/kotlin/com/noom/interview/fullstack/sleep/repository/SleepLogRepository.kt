package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.MorningFeeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@Repository
class SleepLogRepository(private val jdbcTemplate: JdbcTemplate) {

    private val sleepLogRowMapper = RowMapper<SleepLog> { rs, _ ->
        SleepLog(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            sleepDate = rs.getDate("sleep_date").toLocalDate(),
            bedtime = rs.getTime("bedtime").toLocalTime(),
            wakeTime = rs.getTime("wake_time").toLocalTime(),
            totalTimeInBedMinutes = rs.getInt("total_time_in_bed_minutes"),
            morningFeeling = MorningFeeling.valueOf(rs.getString("morning_feeling")),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    fun save(sleepLog: SleepLog): SleepLog {
        val keyHolder = GeneratedKeyHolder()
        
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(
                """INSERT INTO sleep_logs (user_id, sleep_date, bedtime, wake_time, 
                   total_time_in_bed_minutes, morning_feeling) 
                   VALUES (?, ?, ?, ?, ?, ?::morning_feeling)""",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, sleepLog.userId)
            ps.setDate(2, java.sql.Date.valueOf(sleepLog.sleepDate))
            ps.setTime(3, java.sql.Time.valueOf(sleepLog.bedtime))
            ps.setTime(4, java.sql.Time.valueOf(sleepLog.wakeTime))
            ps.setInt(5, sleepLog.totalTimeInBedMinutes)
            ps.setString(6, sleepLog.morningFeeling.name)
            ps
        }, keyHolder)

        val generatedId = keyHolder.key?.toLong() ?: throw RuntimeException("Failed to get generated ID")
        return findById(generatedId)!!
    }

    fun findById(id: Long): SleepLog? {
        return try {
            jdbcTemplate.queryForObject(
                "SELECT * FROM sleep_logs WHERE id = ?",
                sleepLogRowMapper,
                id
            )
        } catch (e: Exception) {
            null
        }
    }

    fun findByUserIdAndDate(userId: Long, date: LocalDate): SleepLog? {
        return try {
            jdbcTemplate.queryForObject(
                "SELECT * FROM sleep_logs WHERE user_id = ? AND sleep_date = ?",
                sleepLogRowMapper,
                userId,
                java.sql.Date.valueOf(date)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun findLastNightSleep(userId: Long): SleepLog? {
        return try {
            jdbcTemplate.queryForObject(
                """SELECT * FROM sleep_logs 
                   WHERE user_id = ? 
                   ORDER BY sleep_date DESC 
                   LIMIT 1""",
                sleepLogRowMapper,
                userId
            )
        } catch (e: Exception) {
            null
        }
    }

    fun findByUserIdInDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): List<SleepLog> {
        return jdbcTemplate.query(
            """SELECT * FROM sleep_logs 
               WHERE user_id = ? AND sleep_date BETWEEN ? AND ? 
               ORDER BY sleep_date DESC""",
            sleepLogRowMapper,
            userId,
            java.sql.Date.valueOf(startDate),
            java.sql.Date.valueOf(endDate)
        )
    }

    fun calculateAverageTimeInBed(userId: Long, startDate: LocalDate, endDate: LocalDate): Double? {
        return try {
            jdbcTemplate.queryForObject(
                """SELECT AVG(total_time_in_bed_minutes) 
                   FROM sleep_logs 
                   WHERE user_id = ? AND sleep_date BETWEEN ? AND ?""",
                Double::class.java,
                userId,
                java.sql.Date.valueOf(startDate),
                java.sql.Date.valueOf(endDate)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun calculateAverageBedtime(userId: Long, startDate: LocalDate, endDate: LocalDate): LocalTime? {
        return try {
            // Convert times to minutes since midnight for averaging
            val avgMinutes = jdbcTemplate.queryForObject(
                """SELECT AVG(EXTRACT(HOUR FROM bedtime) * 60 + EXTRACT(MINUTE FROM bedtime)) 
                   FROM sleep_logs 
                   WHERE user_id = ? AND sleep_date BETWEEN ? AND ?""",
                Double::class.java,
                userId,
                java.sql.Date.valueOf(startDate),
                java.sql.Date.valueOf(endDate)
            )
            
            avgMinutes?.let {
                val hours = (it / 60).toInt()
                val minutes = (it % 60).toInt()
                LocalTime.of(hours, minutes)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun calculateAverageWakeTime(userId: Long, startDate: LocalDate, endDate: LocalDate): LocalTime? {
        return try {
            // Convert times to minutes since midnight for averaging
            val avgMinutes = jdbcTemplate.queryForObject(
                """SELECT AVG(EXTRACT(HOUR FROM wake_time) * 60 + EXTRACT(MINUTE FROM wake_time)) 
                   FROM sleep_logs 
                   WHERE user_id = ? AND sleep_date BETWEEN ? AND ?""",
                Double::class.java,
                userId,
                java.sql.Date.valueOf(startDate),
                java.sql.Date.valueOf(endDate)
            )
            
            avgMinutes?.let {
                val hours = (it / 60).toInt()
                val minutes = (it % 60).toInt()
                LocalTime.of(hours, minutes)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getMorningFeelingFrequencies(userId: Long, startDate: LocalDate, endDate: LocalDate): Map<MorningFeeling, Int> {
        val results = jdbcTemplate.query(
            """SELECT morning_feeling, COUNT(*) as count 
               FROM sleep_logs 
               WHERE user_id = ? AND sleep_date BETWEEN ? AND ? 
               GROUP BY morning_feeling""",
            { rs, _ ->
                MorningFeeling.valueOf(rs.getString("morning_feeling")) to rs.getInt("count")
            },
            userId,
            java.sql.Date.valueOf(startDate),
            java.sql.Date.valueOf(endDate)
        )
        
        return results.toMap()
    }
}

