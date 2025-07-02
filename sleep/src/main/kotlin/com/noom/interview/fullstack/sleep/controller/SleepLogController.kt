package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepStatistics
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/users/{userId}/sleep")
@CrossOrigin(origins = ["*"])
class SleepLogController(private val sleepLogService: SleepLogService) {

    @PostMapping
    fun createSleepLog(
        @PathVariable userId: Long,
        @Valid @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<SleepLog> {
        return try {
            val sleepLog = sleepLogService.createSleepLog(userId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(sleepLog)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/last-night")
    fun getLastNightSleep(@PathVariable userId: Long): ResponseEntity<SleepLog> {
        return try {
            val sleepLog = sleepLogService.getLastNightSleep(userId)
            if (sleepLog != null) {
                ResponseEntity.ok(sleepLog)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/statistics/30-days")
    fun getLast30DaysStatistics(@PathVariable userId: Long): ResponseEntity<SleepStatistics> {
        return try {
            val statistics = sleepLogService.getLast30DaysStatistics(userId)
            if (statistics != null) {
                ResponseEntity.ok(statistics)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }
}


