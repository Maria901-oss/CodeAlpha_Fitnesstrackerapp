package com.codealpha.fitnesstracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Entry(
    val id: Long, val date: String, val type: String,
    val minutes: Int, val calories: Int, val steps: Int
)

class DbHelper(context: Context) : SQLiteOpenHelper(context, "fittrack.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE logs(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, " +
                "type TEXT NOT NULL, minutes INTEGER NOT NULL, calories INTEGER NOT NULL, steps INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {}

    fun add(date: String, type: String, minutes: Int, calories: Int, steps: Int) {
        val cv = ContentValues().apply {
            put("date", date); put("type", type)
            put("minutes", minutes); put("calories", calories); put("steps", steps)
        }
        writableDatabase.insert("logs", null, cv)
    }

    fun delete(id: Long) {
        writableDatabase.delete("logs", "id=?", arrayOf(id.toString()))
    }

    fun clearAll() {
        writableDatabase.delete("logs", null, null)
    }

    /** Returns [steps, calories, minutes, workoutCount] for one date. */
    fun totals(date: String): IntArray {
        val r = intArrayOf(0, 0, 0, 0)
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(steps),0), COALESCE(SUM(calories),0), COALESCE(SUM(minutes),0), COUNT(*) " +
                "FROM logs WHERE date=?", arrayOf(date)
        ).use { c ->
            if (c.moveToFirst()) for (i in 0..3) r[i] = c.getInt(i)
        }
        return r
    }

    fun recent(limit: Int): List<Entry> {
        val out = ArrayList<Entry>()
        readableDatabase.query(
            "logs", null, null, null, null, null, "date DESC, id DESC", limit.toString()
        ).use { c ->
            while (c.moveToNext()) {
                out.add(
                    Entry(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("date")),
                        c.getString(c.getColumnIndexOrThrow("type")),
                        c.getInt(c.getColumnIndexOrThrow("minutes")),
                        c.getInt(c.getColumnIndexOrThrow("calories")),
                        c.getInt(c.getColumnIndexOrThrow("steps"))
                    )
                )
            }
        }
        return out
    }
}
