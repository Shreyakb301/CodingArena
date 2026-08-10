package com.codingarena.core.common

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Injected time source.
 *
 * Every engine that cares about "today" takes one of these rather than reading
 * the system clock, which is what makes streaks, spaced repetition and daily
 * puzzles testable without sleeping.
 */
interface TimeProvider {
    fun nowMillis(): Long
    fun timeZone(): TimeZone

    /** Local calendar day as an epoch day number. */
    fun epochDay(atMillis: Long = nowMillis()): Long =
        Instant.fromEpochMilliseconds(atMillis)
            .toLocalDateTime(timeZone())
            .date
            .toEpochDays()
            .toLong()
}

class SystemTimeProvider(
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) : TimeProvider {
    override fun nowMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    override fun timeZone(): TimeZone = zone
}

/** Test double: time only moves when you move it. */
class FixedTimeProvider(
    var millis: Long = 0L,
    private val zone: TimeZone = TimeZone.UTC,
) : TimeProvider {
    override fun nowMillis(): Long = millis
    override fun timeZone(): TimeZone = zone

    fun advanceDays(days: Long) {
        millis += days * MILLIS_PER_DAY
    }

    fun advanceMinutes(minutes: Long) {
        millis += minutes * 60_000L
    }

    companion object {
        const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
    }
}

const val MILLIS_PER_DAY: Long = 24L * 60 * 60 * 1000
