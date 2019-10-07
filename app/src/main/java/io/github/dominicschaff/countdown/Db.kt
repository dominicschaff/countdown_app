package io.github.dominicschaff.countdown

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.Duration
import java.util.*


data class CountdownTimer(
    val id: Long,
    val date: Long,
    val description: String
)

fun CountdownTimer.farAway(): String {
    val now = Date()
    val then = Date(date)
    if (then.before(now)) return "Date is passed"
    var diff = Duration.between(now.toInstant(), then.toInstant())
    val days = diff.toDays()
    diff = diff.minusDays(days)
    val hours = diff.toHours()
    diff = diff.minusHours(hours)
    val minutes = diff.toMinutes()
    diff = diff.minusMinutes(minutes)
    val seconds = diff.toMillis() / 1000

    var s = if (days == 1L) "$days day" else "$days days"
    when (hours) {
        0L -> {
        }
        1L -> s += " 1 hour"
        else -> s += " $hours hours"
    }
    when (minutes) {
        0L -> {
        }
        1L -> s += " 1 minute"
        else -> s += " $minutes minutes"
    }
    when (seconds) {
        0L -> {
        }
        1L -> s += " 1 second"
        else -> s += " $seconds seconds"
    }

    return s
}


const val DATABASE_VERSION = 1
const val DATABASE_NAME = "timers.db"

class Db(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE timers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER, description TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS timers")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun add(countdownTimer: CountdownTimer) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("date", countdownTimer.date)
            put("description", countdownTimer.description)
        }

        db.insert("timers", null, values)
    }

    fun get(): Array<CountdownTimer> {
        val db = readableDatabase

        val cursor = db.query(
            "timers",
            arrayOf("id", "date", "description"),
            null,
            null,
            null,
            null,
            "id ASC"
        )
        val books = mutableListOf<CountdownTimer>()
        with(cursor) {
            while (moveToNext()) {
                books.add(
                    CountdownTimer(
                        getLong(0),
                        getLong(1),
                        getString(2)
                    )
                )
            }
        }
        return books.toTypedArray()
    }

    fun delete(countdownTimer: CountdownTimer): Boolean =
        writableDatabase.delete("timers", "id=${countdownTimer.id}", null) > 0
}