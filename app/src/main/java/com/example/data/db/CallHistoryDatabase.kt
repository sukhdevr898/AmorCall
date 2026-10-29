package com.example.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.model.CallRecord
import com.example.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Local persistent database for storing Audio and Video Call History
 * between paired users with timestamps, duration, and call status.
 */
class CallHistoryDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "duora_calls.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_CALLS = "calls"
        private const val COL_ID = "id"
        private const val COL_TYPE = "type"
        private const val COL_TIMESTAMP = "timestamp"
        private const val COL_DURATION = "duration_seconds"
        private const val COL_IS_OUTGOING = "is_outgoing"
        private const val COL_PARTNER_CODE = "partner_code"
        private const val COL_PARTNER_NAME = "partner_name"
        private const val COL_PARTNER_AVATAR = "partner_avatar_url"

        @Volatile
        private var instance: CallHistoryDatabase? = null

        fun getInstance(context: Context): CallHistoryDatabase {
            return instance ?: synchronized(this) {
                instance ?: CallHistoryDatabase(context.applicationContext).also { instance = it }
            }
        }
    }

    private val changeListeners = mutableSetOf<() -> Unit>()

    private fun notifyDataChanged() {
        val listeners = synchronized(changeListeners) { changeListeners.toList() }
        listeners.forEach { it.invoke() }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSql = """
            CREATE TABLE $TABLE_CALLS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TYPE TEXT NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL,
                $COL_DURATION INTEGER NOT NULL,
                $COL_IS_OUTGOING INTEGER NOT NULL,
                $COL_PARTNER_CODE TEXT,
                $COL_PARTNER_NAME TEXT,
                $COL_PARTNER_AVATAR TEXT
            )
        """.trimIndent()
        db.execSQL(createSql)
        db.execSQL("CREATE INDEX idx_calls_timestamp ON $TABLE_CALLS ($COL_TIMESTAMP DESC)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CALLS")
        onCreate(db)
    }

    suspend fun insertCall(record: CallRecord, partnerCode: String? = null, partnerName: String? = null, partnerAvatar: String? = null) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(COL_ID, record.id)
            put(COL_TYPE, record.type.name)
            put(COL_TIMESTAMP, record.timestamp)
            put(COL_DURATION, record.durationSeconds)
            put(COL_IS_OUTGOING, if (record.isOutgoing) 1 else 0)
            put(COL_PARTNER_CODE, partnerCode ?: "")
            put(COL_PARTNER_NAME, partnerName ?: "")
            put(COL_PARTNER_AVATAR, partnerAvatar ?: "")
        }
        writableDatabase.insertWithOnConflict(TABLE_CALLS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        notifyDataChanged()
    }

    suspend fun deleteCall(id: String) = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_CALLS, "$COL_ID = ?", arrayOf(id))
        notifyDataChanged()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        writableDatabase.delete(TABLE_CALLS, null, null)
        notifyDataChanged()
    }

    fun getAllCalls(): Flow<List<CallRecord>> = callbackFlow {
        val listener = {
            val list = queryCallsSync()
            trySend(list)
            Unit
        }

        synchronized(changeListeners) {
            changeListeners.add(listener)
        }

        // Emit initial value
        trySend(queryCallsSync())

        awaitClose {
            synchronized(changeListeners) {
                changeListeners.remove(listener)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun queryCallsSync(): List<CallRecord> {
        val list = mutableListOf<CallRecord>()
        var cursor: Cursor? = null
        try {
            cursor = readableDatabase.query(
                TABLE_CALLS,
                null,
                null,
                null,
                null,
                null,
                "$COL_TIMESTAMP DESC",
                "100"
            )
            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID))
                val typeStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP))
                val duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION))
                val isOutgoing = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_OUTGOING)) == 1

                val callType = try {
                    CallType.valueOf(typeStr)
                } catch (_: Exception) {
                    CallType.VOICE
                }

                list.add(
                    CallRecord(
                        id = id,
                        type = callType,
                        timestamp = timestamp,
                        durationSeconds = duration,
                        isOutgoing = isOutgoing
                    )
                )
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return list
    }
}
