package com.lifevault.app.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

/**
 * Section 5.1: dates without time are stored as epoch day (timezone-proof — a document
 * expiring on a calendar date expires on that date wherever the user travels). Instants
 * are stored as epoch millis UTC. Room enums are natively supported since 2.6 and need
 * no converter here.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(epochMillis: Long?): Instant? = epochMillis?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? = list?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toIntList(json: String?): List<Int>? = json?.let { Json.decodeFromString(it) }
}
