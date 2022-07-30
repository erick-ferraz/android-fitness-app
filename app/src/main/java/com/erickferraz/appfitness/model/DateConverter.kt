package com.erickferraz.appfitness.model

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object DateConverter {

    @TypeConverter
    fun toDate(dateTime: Long?) : Date? {
        return if (dateTime != null) Date(dateTime) else null
    }

    @TypeConverter
    fun fromDate(date: Date?) : Long? {
        return date?.time
    }
}