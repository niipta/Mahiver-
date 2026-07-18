package com.example.`data`.sync

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.`data`.SyncQueueEntity
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
public class SyncDao_Impl(
  __db: RoomDatabase,
) : SyncDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSyncQueueEntity: EntityInsertAdapter<SyncQueueEntity>

  private val __deleteAdapterOfSyncQueueEntity: EntityDeleteOrUpdateAdapter<SyncQueueEntity>

  private val __updateAdapterOfSyncQueueEntity: EntityDeleteOrUpdateAdapter<SyncQueueEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSyncQueueEntity = object : EntityInsertAdapter<SyncQueueEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `sync_queue` (`id`,`operationType`,`entityType`,`entityId`,`syncStatus`,`retryCount`,`queuedAt`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SyncQueueEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.operationType)
        statement.bindText(3, entity.entityType)
        statement.bindText(4, entity.entityId)
        statement.bindText(5, entity.syncStatus)
        statement.bindLong(6, entity.retryCount.toLong())
        statement.bindLong(7, entity.queuedAt)
      }
    }
    this.__deleteAdapterOfSyncQueueEntity = object : EntityDeleteOrUpdateAdapter<SyncQueueEntity>()
        {
      protected override fun createQuery(): String = "DELETE FROM `sync_queue` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SyncQueueEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfSyncQueueEntity = object : EntityDeleteOrUpdateAdapter<SyncQueueEntity>()
        {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `sync_queue` SET `id` = ?,`operationType` = ?,`entityType` = ?,`entityId` = ?,`syncStatus` = ?,`retryCount` = ?,`queuedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SyncQueueEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.operationType)
        statement.bindText(3, entity.entityType)
        statement.bindText(4, entity.entityId)
        statement.bindText(5, entity.syncStatus)
        statement.bindLong(6, entity.retryCount.toLong())
        statement.bindLong(7, entity.queuedAt)
        statement.bindText(8, entity.id)
      }
    }
  }

  public override suspend fun insertSyncTask(task: SyncQueueEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfSyncQueueEntity.insert(_connection, task)
  }

  public override suspend fun insertSyncTasks(tasks: List<SyncQueueEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSyncQueueEntity.insert(_connection, tasks)
  }

  public override suspend fun deleteSyncTask(task: SyncQueueEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfSyncQueueEntity.handle(_connection, task)
  }

  public override suspend fun updateSyncTask(task: SyncQueueEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfSyncQueueEntity.handle(_connection, task)
  }

  public override fun getPendingSyncQueue(): Flow<List<SyncQueueEntity>> {
    val _sql: String = "SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY queuedAt ASC"
    return createFlow(__db, false, arrayOf("sync_queue")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOperationType: Int = getColumnIndexOrThrow(_stmt, "operationType")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfRetryCount: Int = getColumnIndexOrThrow(_stmt, "retryCount")
        val _columnIndexOfQueuedAt: Int = getColumnIndexOrThrow(_stmt, "queuedAt")
        val _result: MutableList<SyncQueueEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SyncQueueEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOperationType: String
          _tmpOperationType = _stmt.getText(_columnIndexOfOperationType)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          val _tmpQueuedAt: Long
          _tmpQueuedAt = _stmt.getLong(_columnIndexOfQueuedAt)
          _item =
              SyncQueueEntity(_tmpId,_tmpOperationType,_tmpEntityType,_tmpEntityId,_tmpSyncStatus,_tmpRetryCount,_tmpQueuedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingSyncQueueSync(): List<SyncQueueEntity> {
    val _sql: String = "SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY queuedAt ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfOperationType: Int = getColumnIndexOrThrow(_stmt, "operationType")
        val _columnIndexOfEntityType: Int = getColumnIndexOrThrow(_stmt, "entityType")
        val _columnIndexOfEntityId: Int = getColumnIndexOrThrow(_stmt, "entityId")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfRetryCount: Int = getColumnIndexOrThrow(_stmt, "retryCount")
        val _columnIndexOfQueuedAt: Int = getColumnIndexOrThrow(_stmt, "queuedAt")
        val _result: MutableList<SyncQueueEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SyncQueueEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpOperationType: String
          _tmpOperationType = _stmt.getText(_columnIndexOfOperationType)
          val _tmpEntityType: String
          _tmpEntityType = _stmt.getText(_columnIndexOfEntityType)
          val _tmpEntityId: String
          _tmpEntityId = _stmt.getText(_columnIndexOfEntityId)
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpRetryCount: Int
          _tmpRetryCount = _stmt.getLong(_columnIndexOfRetryCount).toInt()
          val _tmpQueuedAt: Long
          _tmpQueuedAt = _stmt.getLong(_columnIndexOfQueuedAt)
          _item =
              SyncQueueEntity(_tmpId,_tmpOperationType,_tmpEntityType,_tmpEntityId,_tmpSyncStatus,_tmpRetryCount,_tmpQueuedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearCompletedTasks() {
    val _sql: String = "DELETE FROM sync_queue WHERE syncStatus = 'COMPLETED'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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
