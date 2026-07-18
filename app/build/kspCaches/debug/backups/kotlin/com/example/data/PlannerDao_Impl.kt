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
public class PlannerDao_Impl(
  __db: RoomDatabase,
) : PlannerDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDailyPlanEntity: EntityInsertAdapter<DailyPlanEntity>

  private val __deleteAdapterOfDailyPlanEntity: EntityDeleteOrUpdateAdapter<DailyPlanEntity>

  private val __updateAdapterOfDailyPlanEntity: EntityDeleteOrUpdateAdapter<DailyPlanEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDailyPlanEntity = object : EntityInsertAdapter<DailyPlanEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `daily_plans` (`dateString`,`dateMillis`,`plannedTopicIds`,`plannedSubtopicIds`,`plannedRevisionIds`,`isCompleted`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DailyPlanEntity) {
        statement.bindText(1, entity.dateString)
        statement.bindLong(2, entity.dateMillis)
        statement.bindText(3, entity.plannedTopicIds)
        statement.bindText(4, entity.plannedSubtopicIds)
        statement.bindText(5, entity.plannedRevisionIds)
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfDailyPlanEntity = object : EntityDeleteOrUpdateAdapter<DailyPlanEntity>()
        {
      protected override fun createQuery(): String =
          "DELETE FROM `daily_plans` WHERE `dateString` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DailyPlanEntity) {
        statement.bindText(1, entity.dateString)
      }
    }
    this.__updateAdapterOfDailyPlanEntity = object : EntityDeleteOrUpdateAdapter<DailyPlanEntity>()
        {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `daily_plans` SET `dateString` = ?,`dateMillis` = ?,`plannedTopicIds` = ?,`plannedSubtopicIds` = ?,`plannedRevisionIds` = ?,`isCompleted` = ? WHERE `dateString` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DailyPlanEntity) {
        statement.bindText(1, entity.dateString)
        statement.bindLong(2, entity.dateMillis)
        statement.bindText(3, entity.plannedTopicIds)
        statement.bindText(4, entity.plannedSubtopicIds)
        statement.bindText(5, entity.plannedRevisionIds)
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.dateString)
      }
    }
  }

  public override suspend fun insertPlan(plan: DailyPlanEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfDailyPlanEntity.insert(_connection, plan)
  }

  public override suspend fun deletePlan(plan: DailyPlanEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfDailyPlanEntity.handle(_connection, plan)
  }

  public override suspend fun updatePlan(plan: DailyPlanEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfDailyPlanEntity.handle(_connection, plan)
  }

  public override fun getAllDailyPlansSync(): List<DailyPlanEntity> {
    val _sql: String = "SELECT * FROM daily_plans"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDateString: Int = getColumnIndexOrThrow(_stmt, "dateString")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfPlannedTopicIds: Int = getColumnIndexOrThrow(_stmt, "plannedTopicIds")
        val _columnIndexOfPlannedSubtopicIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedSubtopicIds")
        val _columnIndexOfPlannedRevisionIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedRevisionIds")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: MutableList<DailyPlanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DailyPlanEntity
          val _tmpDateString: String
          _tmpDateString = _stmt.getText(_columnIndexOfDateString)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpPlannedTopicIds: String
          _tmpPlannedTopicIds = _stmt.getText(_columnIndexOfPlannedTopicIds)
          val _tmpPlannedSubtopicIds: String
          _tmpPlannedSubtopicIds = _stmt.getText(_columnIndexOfPlannedSubtopicIds)
          val _tmpPlannedRevisionIds: String
          _tmpPlannedRevisionIds = _stmt.getText(_columnIndexOfPlannedRevisionIds)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _item =
              DailyPlanEntity(_tmpDateString,_tmpDateMillis,_tmpPlannedTopicIds,_tmpPlannedSubtopicIds,_tmpPlannedRevisionIds,_tmpIsCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPlan(dateString: String): Flow<DailyPlanEntity?> {
    val _sql: String = "SELECT * FROM daily_plans WHERE dateString = ?"
    return createFlow(__db, false, arrayOf("daily_plans")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dateString)
        val _columnIndexOfDateString: Int = getColumnIndexOrThrow(_stmt, "dateString")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfPlannedTopicIds: Int = getColumnIndexOrThrow(_stmt, "plannedTopicIds")
        val _columnIndexOfPlannedSubtopicIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedSubtopicIds")
        val _columnIndexOfPlannedRevisionIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedRevisionIds")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: DailyPlanEntity?
        if (_stmt.step()) {
          val _tmpDateString: String
          _tmpDateString = _stmt.getText(_columnIndexOfDateString)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpPlannedTopicIds: String
          _tmpPlannedTopicIds = _stmt.getText(_columnIndexOfPlannedTopicIds)
          val _tmpPlannedSubtopicIds: String
          _tmpPlannedSubtopicIds = _stmt.getText(_columnIndexOfPlannedSubtopicIds)
          val _tmpPlannedRevisionIds: String
          _tmpPlannedRevisionIds = _stmt.getText(_columnIndexOfPlannedRevisionIds)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _result =
              DailyPlanEntity(_tmpDateString,_tmpDateMillis,_tmpPlannedTopicIds,_tmpPlannedSubtopicIds,_tmpPlannedRevisionIds,_tmpIsCompleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlanSync(dateString: String): DailyPlanEntity? {
    val _sql: String = "SELECT * FROM daily_plans WHERE dateString = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dateString)
        val _columnIndexOfDateString: Int = getColumnIndexOrThrow(_stmt, "dateString")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfPlannedTopicIds: Int = getColumnIndexOrThrow(_stmt, "plannedTopicIds")
        val _columnIndexOfPlannedSubtopicIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedSubtopicIds")
        val _columnIndexOfPlannedRevisionIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedRevisionIds")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: DailyPlanEntity?
        if (_stmt.step()) {
          val _tmpDateString: String
          _tmpDateString = _stmt.getText(_columnIndexOfDateString)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpPlannedTopicIds: String
          _tmpPlannedTopicIds = _stmt.getText(_columnIndexOfPlannedTopicIds)
          val _tmpPlannedSubtopicIds: String
          _tmpPlannedSubtopicIds = _stmt.getText(_columnIndexOfPlannedSubtopicIds)
          val _tmpPlannedRevisionIds: String
          _tmpPlannedRevisionIds = _stmt.getText(_columnIndexOfPlannedRevisionIds)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _result =
              DailyPlanEntity(_tmpDateString,_tmpDateMillis,_tmpPlannedTopicIds,_tmpPlannedSubtopicIds,_tmpPlannedRevisionIds,_tmpIsCompleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPlansInRange(startDate: String, endDate: String):
      Flow<List<DailyPlanEntity>> {
    val _sql: String = "SELECT * FROM daily_plans WHERE dateString >= ? AND dateString <= ?"
    return createFlow(__db, false, arrayOf("daily_plans")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, startDate)
        _argIndex = 2
        _stmt.bindText(_argIndex, endDate)
        val _columnIndexOfDateString: Int = getColumnIndexOrThrow(_stmt, "dateString")
        val _columnIndexOfDateMillis: Int = getColumnIndexOrThrow(_stmt, "dateMillis")
        val _columnIndexOfPlannedTopicIds: Int = getColumnIndexOrThrow(_stmt, "plannedTopicIds")
        val _columnIndexOfPlannedSubtopicIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedSubtopicIds")
        val _columnIndexOfPlannedRevisionIds: Int = getColumnIndexOrThrow(_stmt,
            "plannedRevisionIds")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: MutableList<DailyPlanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DailyPlanEntity
          val _tmpDateString: String
          _tmpDateString = _stmt.getText(_columnIndexOfDateString)
          val _tmpDateMillis: Long
          _tmpDateMillis = _stmt.getLong(_columnIndexOfDateMillis)
          val _tmpPlannedTopicIds: String
          _tmpPlannedTopicIds = _stmt.getText(_columnIndexOfPlannedTopicIds)
          val _tmpPlannedSubtopicIds: String
          _tmpPlannedSubtopicIds = _stmt.getText(_columnIndexOfPlannedSubtopicIds)
          val _tmpPlannedRevisionIds: String
          _tmpPlannedRevisionIds = _stmt.getText(_columnIndexOfPlannedRevisionIds)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _item =
              DailyPlanEntity(_tmpDateString,_tmpDateMillis,_tmpPlannedTopicIds,_tmpPlannedSubtopicIds,_tmpPlannedRevisionIds,_tmpIsCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePlanCompletion(dateString: String, completed: Boolean) {
    val _sql: String = "UPDATE daily_plans SET isCompleted = ? WHERE dateString = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, dateString)
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
