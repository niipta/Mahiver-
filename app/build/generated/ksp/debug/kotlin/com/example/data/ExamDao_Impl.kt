package com.example.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class ExamDao_Impl(
  __db: RoomDatabase,
) : ExamDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExamEntity: EntityInsertAdapter<ExamEntity>

  private val __deleteAdapterOfExamEntity: EntityDeleteOrUpdateAdapter<ExamEntity>

  private val __updateAdapterOfExamEntity: EntityDeleteOrUpdateAdapter<ExamEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfExamEntity = object : EntityInsertAdapter<ExamEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `exams` (`id`,`name`,`dateMillis`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExamEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.dateMillis)
      }
    }
    this.__deleteAdapterOfExamEntity = object : EntityDeleteOrUpdateAdapter<ExamEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `exams` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExamEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfExamEntity = object : EntityDeleteOrUpdateAdapter<ExamEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `exams` SET `id` = ?,`name` = ?,`dateMillis` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExamEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.dateMillis)
        statement.bindText(4, entity.id)
      }
    }
  }

  public override suspend fun insertExam(exam: ExamEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfExamEntity.insert(_connection, exam)
  }

  public override suspend fun deleteExam(exam: ExamEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfExamEntity.handle(_connection, exam)
  }

  public override suspend fun updateExam(exam: ExamEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfExamEntity.handle(_connection, exam)
  }

  public override fun getAllExams(): Flow<List<ExamEntity>> {
    val _sql: String = "SELECT * FROM exams ORDER BY dateMillis ASC"
    return createFlow(__db, false, arrayOf("exams")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _result: MutableList<ExamEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExamEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          _item = ExamEntity(_tmpId,_tmpName,_tmpDateMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllExamsSync(): List<ExamEntity> {
    val _sql: String = "SELECT * FROM exams ORDER BY dateMillis ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _result: MutableList<ExamEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExamEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          _item = ExamEntity(_tmpId,_tmpName,_tmpDateMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExamById(id: String): ExamEntity? {
    val _sql: String = "SELECT * FROM exams WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _result: ExamEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          _result = ExamEntity(_tmpId,_tmpName,_tmpDateMillis)
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
