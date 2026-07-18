package com.example.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
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
public class RevisionDao_Impl(
  __db: RoomDatabase,
) : RevisionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfRevisionEntity: EntityInsertAdapter<RevisionEntity>

  private val __deleteAdapterOfRevisionEntity: EntityDeleteOrUpdateAdapter<RevisionEntity>

  private val __updateAdapterOfRevisionEntity: EntityDeleteOrUpdateAdapter<RevisionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfRevisionEntity = object : EntityInsertAdapter<RevisionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `revisions` (`id`,`relatedId`,`subjectName`,`title`,`type`,`priority`,`estimatedMinutes`,`scheduledDateMillis`,`isCompleted`,`isActive`,`confidence`,`repetitionLevel`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: RevisionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.relatedId)
        statement.bindText(3, entity.subjectName)
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.type)
        statement.bindText(6, entity.priority)
        statement.bindLong(7, entity.estimatedMinutes.toLong())
        statement.bindLong(8, entity.scheduledDateMillis)
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindLong(11, entity.confidence.toLong())
        statement.bindLong(12, entity.repetitionLevel.toLong())
      }
    }
    this.__deleteAdapterOfRevisionEntity = object : EntityDeleteOrUpdateAdapter<RevisionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `revisions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: RevisionEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfRevisionEntity = object : EntityDeleteOrUpdateAdapter<RevisionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `revisions` SET `id` = ?,`relatedId` = ?,`subjectName` = ?,`title` = ?,`type` = ?,`priority` = ?,`estimatedMinutes` = ?,`scheduledDateMillis` = ?,`isCompleted` = ?,`isActive` = ?,`confidence` = ?,`repetitionLevel` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: RevisionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.relatedId)
        statement.bindText(3, entity.subjectName)
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.type)
        statement.bindText(6, entity.priority)
        statement.bindLong(7, entity.estimatedMinutes.toLong())
        statement.bindLong(8, entity.scheduledDateMillis)
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        val _tmp_1: Int = if (entity.isActive) 1 else 0
        statement.bindLong(10, _tmp_1.toLong())
        statement.bindLong(11, entity.confidence.toLong())
        statement.bindLong(12, entity.repetitionLevel.toLong())
        statement.bindText(13, entity.id)
      }
    }
  }

  public override suspend fun insertRevision(revision: RevisionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfRevisionEntity.insert(_connection, revision)
  }

  public override suspend fun deleteRevision(revision: RevisionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfRevisionEntity.handle(_connection, revision)
  }

  public override suspend fun updateRevision(revision: RevisionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfRevisionEntity.handle(_connection, revision)
  }

  public override fun getAllRevisionsSync(): List<RevisionEntity> {
    val _sql: String = "SELECT * FROM revisions"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRelatedId: Int = getColumnIndexOrThrow(_stmt, "relatedId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfScheduledDateMillis: Int = getColumnIndexOrThrow(_stmt,
            "scheduledDateMillis")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfRepetitionLevel: Int = getColumnIndexOrThrow(_stmt, "repetitionLevel")
        val _result: MutableList<RevisionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RevisionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRelatedId: String
          _tmpRelatedId = _stmt.getText(_columnIndexOfRelatedId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpScheduledDateMillis: Long
          _tmpScheduledDateMillis = _stmt.getLong(_columnIndexOfScheduledDateMillis)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpConfidence: Int
          _tmpConfidence = _stmt.getLong(_columnIndexOfConfidence).toInt()
          val _tmpRepetitionLevel: Int
          _tmpRepetitionLevel = _stmt.getLong(_columnIndexOfRepetitionLevel).toInt()
          _item =
              RevisionEntity(_tmpId,_tmpRelatedId,_tmpSubjectName,_tmpTitle,_tmpType,_tmpPriority,_tmpEstimatedMinutes,_tmpScheduledDateMillis,_tmpIsCompleted,_tmpIsActive,_tmpConfidence,_tmpRepetitionLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllRevisions(): Flow<List<RevisionEntity>> {
    val _sql: String = "SELECT * FROM revisions ORDER BY scheduledDateMillis ASC"
    return createFlow(__db, false, arrayOf("revisions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRelatedId: Int = getColumnIndexOrThrow(_stmt, "relatedId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfScheduledDateMillis: Int = getColumnIndexOrThrow(_stmt,
            "scheduledDateMillis")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfRepetitionLevel: Int = getColumnIndexOrThrow(_stmt, "repetitionLevel")
        val _result: MutableList<RevisionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RevisionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRelatedId: String
          _tmpRelatedId = _stmt.getText(_columnIndexOfRelatedId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpScheduledDateMillis: Long
          _tmpScheduledDateMillis = _stmt.getLong(_columnIndexOfScheduledDateMillis)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpConfidence: Int
          _tmpConfidence = _stmt.getLong(_columnIndexOfConfidence).toInt()
          val _tmpRepetitionLevel: Int
          _tmpRepetitionLevel = _stmt.getLong(_columnIndexOfRepetitionLevel).toInt()
          _item =
              RevisionEntity(_tmpId,_tmpRelatedId,_tmpSubjectName,_tmpTitle,_tmpType,_tmpPriority,_tmpEstimatedMinutes,_tmpScheduledDateMillis,_tmpIsCompleted,_tmpIsActive,_tmpConfidence,_tmpRepetitionLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingRevisionsSync(): List<RevisionEntity> {
    val _sql: String =
        "SELECT * FROM revisions WHERE isActive = 1 AND isCompleted = 0 ORDER BY scheduledDateMillis ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRelatedId: Int = getColumnIndexOrThrow(_stmt, "relatedId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfScheduledDateMillis: Int = getColumnIndexOrThrow(_stmt,
            "scheduledDateMillis")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfRepetitionLevel: Int = getColumnIndexOrThrow(_stmt, "repetitionLevel")
        val _result: MutableList<RevisionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RevisionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRelatedId: String
          _tmpRelatedId = _stmt.getText(_columnIndexOfRelatedId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpScheduledDateMillis: Long
          _tmpScheduledDateMillis = _stmt.getLong(_columnIndexOfScheduledDateMillis)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpConfidence: Int
          _tmpConfidence = _stmt.getLong(_columnIndexOfConfidence).toInt()
          val _tmpRepetitionLevel: Int
          _tmpRepetitionLevel = _stmt.getLong(_columnIndexOfRepetitionLevel).toInt()
          _item =
              RevisionEntity(_tmpId,_tmpRelatedId,_tmpSubjectName,_tmpTitle,_tmpType,_tmpPriority,_tmpEstimatedMinutes,_tmpScheduledDateMillis,_tmpIsCompleted,_tmpIsActive,_tmpConfidence,_tmpRepetitionLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRevisionsByRelatedId(relatedId: String): List<RevisionEntity> {
    val _sql: String = "SELECT * FROM revisions WHERE relatedId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, relatedId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRelatedId: Int = getColumnIndexOrThrow(_stmt, "relatedId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfScheduledDateMillis: Int = getColumnIndexOrThrow(_stmt,
            "scheduledDateMillis")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfRepetitionLevel: Int = getColumnIndexOrThrow(_stmt, "repetitionLevel")
        val _result: MutableList<RevisionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RevisionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRelatedId: String
          _tmpRelatedId = _stmt.getText(_columnIndexOfRelatedId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpScheduledDateMillis: Long
          _tmpScheduledDateMillis = _stmt.getLong(_columnIndexOfScheduledDateMillis)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpConfidence: Int
          _tmpConfidence = _stmt.getLong(_columnIndexOfConfidence).toInt()
          val _tmpRepetitionLevel: Int
          _tmpRepetitionLevel = _stmt.getLong(_columnIndexOfRepetitionLevel).toInt()
          _item =
              RevisionEntity(_tmpId,_tmpRelatedId,_tmpSubjectName,_tmpTitle,_tmpType,_tmpPriority,_tmpEstimatedMinutes,_tmpScheduledDateMillis,_tmpIsCompleted,_tmpIsActive,_tmpConfidence,_tmpRepetitionLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRevisionById(id: String): RevisionEntity? {
    val _sql: String = "SELECT * FROM revisions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRelatedId: Int = getColumnIndexOrThrow(_stmt, "relatedId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfScheduledDateMillis: Int = getColumnIndexOrThrow(_stmt,
            "scheduledDateMillis")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfRepetitionLevel: Int = getColumnIndexOrThrow(_stmt, "repetitionLevel")
        val _result: RevisionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRelatedId: String
          _tmpRelatedId = _stmt.getText(_columnIndexOfRelatedId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpScheduledDateMillis: Long
          _tmpScheduledDateMillis = _stmt.getLong(_columnIndexOfScheduledDateMillis)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpConfidence: Int
          _tmpConfidence = _stmt.getLong(_columnIndexOfConfidence).toInt()
          val _tmpRepetitionLevel: Int
          _tmpRepetitionLevel = _stmt.getLong(_columnIndexOfRepetitionLevel).toInt()
          _result =
              RevisionEntity(_tmpId,_tmpRelatedId,_tmpSubjectName,_tmpTitle,_tmpType,_tmpPriority,_tmpEstimatedMinutes,_tmpScheduledDateMillis,_tmpIsCompleted,_tmpIsActive,_tmpConfidence,_tmpRepetitionLevel)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getRevisionsInRange(startMs: Long, endMs: Long): Flow<List<RevisionEntity>> {
    val _sql: String =
        "SELECT * FROM revisions WHERE scheduledDateMillis >= ? AND scheduledDateMillis < ?"
    return createFlow(__db, false, arrayOf("revisions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startMs)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endMs)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfRelatedId: Int = getColumnIndexOrThrow(_stmt, "relatedId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfScheduledDateMillis: Int = getColumnIndexOrThrow(_stmt,
            "scheduledDateMillis")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfRepetitionLevel: Int = getColumnIndexOrThrow(_stmt, "repetitionLevel")
        val _result: MutableList<RevisionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: RevisionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpRelatedId: String
          _tmpRelatedId = _stmt.getText(_columnIndexOfRelatedId)
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpPriority: String
          _tmpPriority = _stmt.getText(_columnIndexOfPriority)
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpScheduledDateMillis: Long
          _tmpScheduledDateMillis = _stmt.getLong(_columnIndexOfScheduledDateMillis)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpIsActive: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_1 != 0
          val _tmpConfidence: Int
          _tmpConfidence = _stmt.getLong(_columnIndexOfConfidence).toInt()
          val _tmpRepetitionLevel: Int
          _tmpRepetitionLevel = _stmt.getLong(_columnIndexOfRepetitionLevel).toInt()
          _item =
              RevisionEntity(_tmpId,_tmpRelatedId,_tmpSubjectName,_tmpTitle,_tmpType,_tmpPriority,_tmpEstimatedMinutes,_tmpScheduledDateMillis,_tmpIsCompleted,_tmpIsActive,_tmpConfidence,_tmpRepetitionLevel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCompletedRevisionsCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM revisions WHERE isCompleted = 1"
    return createFlow(__db, false, arrayOf("revisions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPendingRevisionsCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM revisions WHERE isActive = 1 AND isCompleted = 0"
    return createFlow(__db, false, arrayOf("revisions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteRevisionsByRelatedId(relatedId: String) {
    val _sql: String = "DELETE FROM revisions WHERE relatedId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, relatedId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
