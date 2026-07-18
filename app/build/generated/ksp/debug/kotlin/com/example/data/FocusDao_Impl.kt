package com.example.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FocusDao_Impl(
  __db: RoomDatabase,
) : FocusDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfFocusSessionEntity: EntityInsertAdapter<FocusSessionEntity>

  private val __deleteAdapterOfFocusSessionEntity: EntityDeleteOrUpdateAdapter<FocusSessionEntity>

  private val __updateAdapterOfFocusSessionEntity: EntityDeleteOrUpdateAdapter<FocusSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfFocusSessionEntity = object : EntityInsertAdapter<FocusSessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `focus_sessions` (`id`,`subjectId`,`topicId`,`subtopicId`,`subjectName`,`topicName`,`durationMinutes`,`actualDurationSeconds`,`interruptions`,`sessionType`,`isDeepFocus`,`timestamp`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FocusSessionEntity) {
        statement.bindText(1, entity.id)
        val _tmpSubjectId: String? = entity.subjectId
        if (_tmpSubjectId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpSubjectId)
        }
        val _tmpTopicId: String? = entity.topicId
        if (_tmpTopicId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpTopicId)
        }
        val _tmpSubtopicId: String? = entity.subtopicId
        if (_tmpSubtopicId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpSubtopicId)
        }
        statement.bindText(5, entity.subjectName)
        val _tmpTopicName: String? = entity.topicName
        if (_tmpTopicName == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpTopicName)
        }
        statement.bindLong(7, entity.durationMinutes.toLong())
        statement.bindLong(8, entity.actualDurationSeconds.toLong())
        statement.bindLong(9, entity.interruptions.toLong())
        statement.bindText(10, entity.sessionType)
        val _tmp: Int = if (entity.isDeepFocus) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        statement.bindLong(12, entity.timestamp)
      }
    }
    this.__deleteAdapterOfFocusSessionEntity = object :
        EntityDeleteOrUpdateAdapter<FocusSessionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `focus_sessions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FocusSessionEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfFocusSessionEntity = object :
        EntityDeleteOrUpdateAdapter<FocusSessionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `focus_sessions` SET `id` = ?,`subjectId` = ?,`topicId` = ?,`subtopicId` = ?,`subjectName` = ?,`topicName` = ?,`durationMinutes` = ?,`actualDurationSeconds` = ?,`interruptions` = ?,`sessionType` = ?,`isDeepFocus` = ?,`timestamp` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FocusSessionEntity) {
        statement.bindText(1, entity.id)
        val _tmpSubjectId: String? = entity.subjectId
        if (_tmpSubjectId == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpSubjectId)
        }
        val _tmpTopicId: String? = entity.topicId
        if (_tmpTopicId == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpTopicId)
        }
        val _tmpSubtopicId: String? = entity.subtopicId
        if (_tmpSubtopicId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpSubtopicId)
        }
        statement.bindText(5, entity.subjectName)
        val _tmpTopicName: String? = entity.topicName
        if (_tmpTopicName == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpTopicName)
        }
        statement.bindLong(7, entity.durationMinutes.toLong())
        statement.bindLong(8, entity.actualDurationSeconds.toLong())
        statement.bindLong(9, entity.interruptions.toLong())
        statement.bindText(10, entity.sessionType)
        val _tmp: Int = if (entity.isDeepFocus) 1 else 0
        statement.bindLong(11, _tmp.toLong())
        statement.bindLong(12, entity.timestamp)
        statement.bindText(13, entity.id)
      }
    }
  }

  public override suspend fun insertSession(session: FocusSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfFocusSessionEntity.insert(_connection, session)
  }

  public override suspend fun deleteSession(session: FocusSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfFocusSessionEntity.handle(_connection, session)
  }

  public override suspend fun updateSession(session: FocusSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfFocusSessionEntity.handle(_connection, session)
  }

  public override fun getAllSessions(): Flow<List<FocusSessionEntity>> {
    val _sql: String = "SELECT * FROM focus_sessions ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("focus_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTopicId: Int = getColumnIndexOrThrow(_stmt, "topicId")
        val _columnIndexOfSubtopicId: Int = getColumnIndexOrThrow(_stmt, "subtopicId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTopicName: Int = getColumnIndexOrThrow(_stmt, "topicName")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfActualDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "actualDurationSeconds")
        val _columnIndexOfInterruptions: Int = getColumnIndexOrThrow(_stmt, "interruptions")
        val _columnIndexOfSessionType: Int = getColumnIndexOrThrow(_stmt, "sessionType")
        val _columnIndexOfIsDeepFocus: Int = getColumnIndexOrThrow(_stmt, "isDeepFocus")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FocusSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FocusSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String?
          if (_stmt.isNull(_columnIndexOfSubjectId)) {
            _tmpSubjectId = null
          } else {
            _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          }
          val _tmpTopicId: String?
          if (_stmt.isNull(_columnIndexOfTopicId)) {
            _tmpTopicId = null
          } else {
            _tmpTopicId = _stmt.getText(_columnIndexOfTopicId)
          }
          val _tmpSubtopicId: String?
          if (_stmt.isNull(_columnIndexOfSubtopicId)) {
            _tmpSubtopicId = null
          } else {
            _tmpSubtopicId = _stmt.getText(_columnIndexOfSubtopicId)
          }
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String?
          if (_stmt.isNull(_columnIndexOfTopicName)) {
            _tmpTopicName = null
          } else {
            _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpActualDurationSeconds: Int
          _tmpActualDurationSeconds = _stmt.getLong(_columnIndexOfActualDurationSeconds).toInt()
          val _tmpInterruptions: Int
          _tmpInterruptions = _stmt.getLong(_columnIndexOfInterruptions).toInt()
          val _tmpSessionType: String
          _tmpSessionType = _stmt.getText(_columnIndexOfSessionType)
          val _tmpIsDeepFocus: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeepFocus).toInt()
          _tmpIsDeepFocus = _tmp != 0
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item =
              FocusSessionEntity(_tmpId,_tmpSubjectId,_tmpTopicId,_tmpSubtopicId,_tmpSubjectName,_tmpTopicName,_tmpDurationMinutes,_tmpActualDurationSeconds,_tmpInterruptions,_tmpSessionType,_tmpIsDeepFocus,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllSessionsSync(): List<FocusSessionEntity> {
    val _sql: String = "SELECT * FROM focus_sessions ORDER BY timestamp ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTopicId: Int = getColumnIndexOrThrow(_stmt, "topicId")
        val _columnIndexOfSubtopicId: Int = getColumnIndexOrThrow(_stmt, "subtopicId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTopicName: Int = getColumnIndexOrThrow(_stmt, "topicName")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfActualDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "actualDurationSeconds")
        val _columnIndexOfInterruptions: Int = getColumnIndexOrThrow(_stmt, "interruptions")
        val _columnIndexOfSessionType: Int = getColumnIndexOrThrow(_stmt, "sessionType")
        val _columnIndexOfIsDeepFocus: Int = getColumnIndexOrThrow(_stmt, "isDeepFocus")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<FocusSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FocusSessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String?
          if (_stmt.isNull(_columnIndexOfSubjectId)) {
            _tmpSubjectId = null
          } else {
            _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          }
          val _tmpTopicId: String?
          if (_stmt.isNull(_columnIndexOfTopicId)) {
            _tmpTopicId = null
          } else {
            _tmpTopicId = _stmt.getText(_columnIndexOfTopicId)
          }
          val _tmpSubtopicId: String?
          if (_stmt.isNull(_columnIndexOfSubtopicId)) {
            _tmpSubtopicId = null
          } else {
            _tmpSubtopicId = _stmt.getText(_columnIndexOfSubtopicId)
          }
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String?
          if (_stmt.isNull(_columnIndexOfTopicName)) {
            _tmpTopicName = null
          } else {
            _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpActualDurationSeconds: Int
          _tmpActualDurationSeconds = _stmt.getLong(_columnIndexOfActualDurationSeconds).toInt()
          val _tmpInterruptions: Int
          _tmpInterruptions = _stmt.getLong(_columnIndexOfInterruptions).toInt()
          val _tmpSessionType: String
          _tmpSessionType = _stmt.getText(_columnIndexOfSessionType)
          val _tmpIsDeepFocus: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeepFocus).toInt()
          _tmpIsDeepFocus = _tmp != 0
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item =
              FocusSessionEntity(_tmpId,_tmpSubjectId,_tmpTopicId,_tmpSubtopicId,_tmpSubjectName,_tmpTopicName,_tmpDurationMinutes,_tmpActualDurationSeconds,_tmpInterruptions,_tmpSessionType,_tmpIsDeepFocus,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSessionById(id: String): FocusSessionEntity? {
    val _sql: String = "SELECT * FROM focus_sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfTopicId: Int = getColumnIndexOrThrow(_stmt, "topicId")
        val _columnIndexOfSubtopicId: Int = getColumnIndexOrThrow(_stmt, "subtopicId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTopicName: Int = getColumnIndexOrThrow(_stmt, "topicName")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfActualDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "actualDurationSeconds")
        val _columnIndexOfInterruptions: Int = getColumnIndexOrThrow(_stmt, "interruptions")
        val _columnIndexOfSessionType: Int = getColumnIndexOrThrow(_stmt, "sessionType")
        val _columnIndexOfIsDeepFocus: Int = getColumnIndexOrThrow(_stmt, "isDeepFocus")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: FocusSessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String?
          if (_stmt.isNull(_columnIndexOfSubjectId)) {
            _tmpSubjectId = null
          } else {
            _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          }
          val _tmpTopicId: String?
          if (_stmt.isNull(_columnIndexOfTopicId)) {
            _tmpTopicId = null
          } else {
            _tmpTopicId = _stmt.getText(_columnIndexOfTopicId)
          }
          val _tmpSubtopicId: String?
          if (_stmt.isNull(_columnIndexOfSubtopicId)) {
            _tmpSubtopicId = null
          } else {
            _tmpSubtopicId = _stmt.getText(_columnIndexOfSubtopicId)
          }
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String?
          if (_stmt.isNull(_columnIndexOfTopicName)) {
            _tmpTopicName = null
          } else {
            _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpActualDurationSeconds: Int
          _tmpActualDurationSeconds = _stmt.getLong(_columnIndexOfActualDurationSeconds).toInt()
          val _tmpInterruptions: Int
          _tmpInterruptions = _stmt.getLong(_columnIndexOfInterruptions).toInt()
          val _tmpSessionType: String
          _tmpSessionType = _stmt.getText(_columnIndexOfSessionType)
          val _tmpIsDeepFocus: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsDeepFocus).toInt()
          _tmpIsDeepFocus = _tmp != 0
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result =
              FocusSessionEntity(_tmpId,_tmpSubjectId,_tmpTopicId,_tmpSubtopicId,_tmpSubjectName,_tmpTopicName,_tmpDurationMinutes,_tmpActualDurationSeconds,_tmpInterruptions,_tmpSessionType,_tmpIsDeepFocus,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
