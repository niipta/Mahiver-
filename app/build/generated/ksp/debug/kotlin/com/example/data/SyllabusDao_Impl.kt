package com.example.`data`

import androidx.collection.ArrayMap
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchArrayMap
import androidx.sqlite.SQLiteConnection
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
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SyllabusDao_Impl(
  __db: RoomDatabase,
) : SyllabusDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSubjectEntity: EntityInsertAdapter<SubjectEntity>

  private val __insertAdapterOfTopicEntity: EntityInsertAdapter<TopicEntity>

  private val __insertAdapterOfSubtopicEntity: EntityInsertAdapter<SubtopicEntity>

  private val __deleteAdapterOfSubjectEntity: EntityDeleteOrUpdateAdapter<SubjectEntity>

  private val __deleteAdapterOfTopicEntity: EntityDeleteOrUpdateAdapter<TopicEntity>

  private val __deleteAdapterOfSubtopicEntity: EntityDeleteOrUpdateAdapter<SubtopicEntity>

  private val __updateAdapterOfSubjectEntity: EntityDeleteOrUpdateAdapter<SubjectEntity>

  private val __updateAdapterOfTopicEntity: EntityDeleteOrUpdateAdapter<TopicEntity>

  private val __updateAdapterOfSubtopicEntity: EntityDeleteOrUpdateAdapter<SubtopicEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSubjectEntity = object : EntityInsertAdapter<SubjectEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `subjects` (`id`,`name`,`icon`,`color`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SubjectEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.icon)
        statement.bindLong(4, entity.color)
      }
    }
    this.__insertAdapterOfTopicEntity = object : EntityInsertAdapter<TopicEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `topics` (`id`,`subjectId`,`name`,`isPriority`,`isWeak`,`estimatedMinutes`,`isCompleted`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TopicEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.subjectId)
        statement.bindText(3, entity.name)
        val _tmp: Int = if (entity.isPriority) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.isWeak) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindLong(6, entity.estimatedMinutes.toLong())
        val _tmp_2: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(7, _tmp_2.toLong())
      }
    }
    this.__insertAdapterOfSubtopicEntity = object : EntityInsertAdapter<SubtopicEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `subtopics` (`id`,`topicId`,`name`,`isCompleted`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SubtopicEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.topicId)
        statement.bindText(3, entity.name)
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(4, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfSubjectEntity = object : EntityDeleteOrUpdateAdapter<SubjectEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `subjects` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubjectEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__deleteAdapterOfTopicEntity = object : EntityDeleteOrUpdateAdapter<TopicEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `topics` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TopicEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__deleteAdapterOfSubtopicEntity = object : EntityDeleteOrUpdateAdapter<SubtopicEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `subtopics` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubtopicEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfSubjectEntity = object : EntityDeleteOrUpdateAdapter<SubjectEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `subjects` SET `id` = ?,`name` = ?,`icon` = ?,`color` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubjectEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.icon)
        statement.bindLong(4, entity.color)
        statement.bindText(5, entity.id)
      }
    }
    this.__updateAdapterOfTopicEntity = object : EntityDeleteOrUpdateAdapter<TopicEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `topics` SET `id` = ?,`subjectId` = ?,`name` = ?,`isPriority` = ?,`isWeak` = ?,`estimatedMinutes` = ?,`isCompleted` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TopicEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.subjectId)
        statement.bindText(3, entity.name)
        val _tmp: Int = if (entity.isPriority) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.isWeak) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindLong(6, entity.estimatedMinutes.toLong())
        val _tmp_2: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(7, _tmp_2.toLong())
        statement.bindText(8, entity.id)
      }
    }
    this.__updateAdapterOfSubtopicEntity = object : EntityDeleteOrUpdateAdapter<SubtopicEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `subtopics` SET `id` = ?,`topicId` = ?,`name` = ?,`isCompleted` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubtopicEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.topicId)
        statement.bindText(3, entity.name)
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        statement.bindText(5, entity.id)
      }
    }
  }

  public override suspend fun insertSubject(subject: SubjectEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfSubjectEntity.insert(_connection, subject)
  }

  public override suspend fun insertTopic(topic: TopicEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfTopicEntity.insert(_connection, topic)
  }

  public override suspend fun insertSubtopic(subtopic: SubtopicEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSubtopicEntity.insert(_connection, subtopic)
  }

  public override suspend fun deleteSubject(subject: SubjectEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfSubjectEntity.handle(_connection, subject)
  }

  public override suspend fun deleteTopic(topic: TopicEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfTopicEntity.handle(_connection, topic)
  }

  public override suspend fun deleteSubtopic(subtopic: SubtopicEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSubtopicEntity.handle(_connection, subtopic)
  }

  public override suspend fun updateSubject(subject: SubjectEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfSubjectEntity.handle(_connection, subject)
  }

  public override suspend fun updateTopic(topic: TopicEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfTopicEntity.handle(_connection, topic)
  }

  public override suspend fun updateSubtopic(subtopic: SubtopicEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSubtopicEntity.handle(_connection, subtopic)
  }

  public override fun getAllSubjectsSync(): List<SubjectEntity> {
    val _sql: String = "SELECT * FROM subjects"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _result: MutableList<SubjectEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubjectEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: Long
          _tmpColor = _stmt.getLong(_columnIndexOfColor)
          _item = SubjectEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllTopicsSync(): List<TopicEntity> {
    val _sql: String = "SELECT * FROM topics"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsPriority: Int = getColumnIndexOrThrow(_stmt, "isPriority")
        val _columnIndexOfIsWeak: Int = getColumnIndexOrThrow(_stmt, "isWeak")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: MutableList<TopicEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TopicEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpIsWeak: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsWeak).toInt()
          _tmpIsWeak = _tmp_1 != 0
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          _item =
              TopicEntity(_tmpId,_tmpSubjectId,_tmpName,_tmpIsPriority,_tmpIsWeak,_tmpEstimatedMinutes,_tmpIsCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllSubtopicsSync(): List<SubtopicEntity> {
    val _sql: String = "SELECT * FROM subtopics"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTopicId: Int = getColumnIndexOrThrow(_stmt, "topicId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: MutableList<SubtopicEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubtopicEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTopicId: String
          _tmpTopicId = _stmt.getText(_columnIndexOfTopicId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _item = SubtopicEntity(_tmpId,_tmpTopicId,_tmpName,_tmpIsCompleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllSubjectsWithTopics(): Flow<List<SubjectWithTopics>> {
    val _sql: String = "SELECT * FROM subjects"
    return createFlow(__db, true, arrayOf("subtopics", "topics", "subjects")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _collectionTopics: ArrayMap<String, MutableList<TopicWithSubtopics>> =
            ArrayMap<String, MutableList<TopicWithSubtopics>>()
        while (_stmt.step()) {
          val _tmpKey: String
          _tmpKey = _stmt.getText(_columnIndexOfId)
          if (!_collectionTopics.containsKey(_tmpKey)) {
            _collectionTopics.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshiptopicsAscomExampleDataTopicWithSubtopics(_connection, _collectionTopics)
        val _result: MutableList<SubjectWithTopics> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubjectWithTopics
          val _tmpSubject: SubjectEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: Long
          _tmpColor = _stmt.getLong(_columnIndexOfColor)
          _tmpSubject = SubjectEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor)
          val _tmpTopicsCollection: MutableList<TopicWithSubtopics>
          val _tmpKey_1: String
          _tmpKey_1 = _stmt.getText(_columnIndexOfId)
          _tmpTopicsCollection = _collectionTopics.getValue(_tmpKey_1)
          _item = SubjectWithTopics(_tmpSubject,_tmpTopicsCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSubjectById(id: String): SubjectEntity? {
    val _sql: String = "SELECT * FROM subjects WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIcon: Int = getColumnIndexOrThrow(_stmt, "icon")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _result: SubjectEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIcon: String
          _tmpIcon = _stmt.getText(_columnIndexOfIcon)
          val _tmpColor: Long
          _tmpColor = _stmt.getLong(_columnIndexOfColor)
          _result = SubjectEntity(_tmpId,_tmpName,_tmpIcon,_tmpColor)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTopicById(id: String): TopicEntity? {
    val _sql: String = "SELECT * FROM topics WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsPriority: Int = getColumnIndexOrThrow(_stmt, "isPriority")
        val _columnIndexOfIsWeak: Int = getColumnIndexOrThrow(_stmt, "isWeak")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: TopicEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpIsWeak: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsWeak).toInt()
          _tmpIsWeak = _tmp_1 != 0
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          _result =
              TopicEntity(_tmpId,_tmpSubjectId,_tmpName,_tmpIsPriority,_tmpIsWeak,_tmpEstimatedMinutes,_tmpIsCompleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTopicsByIds(ids: List<String>): List<TopicEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM topics WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in ids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsPriority: Int = getColumnIndexOrThrow(_stmt, "isPriority")
        val _columnIndexOfIsWeak: Int = getColumnIndexOrThrow(_stmt, "isWeak")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: MutableList<TopicEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: TopicEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpIsWeak: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsWeak).toInt()
          _tmpIsWeak = _tmp_1 != 0
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          _item_1 =
              TopicEntity(_tmpId,_tmpSubjectId,_tmpName,_tmpIsPriority,_tmpIsWeak,_tmpEstimatedMinutes,_tmpIsCompleted)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSubtopicById(id: String): SubtopicEntity? {
    val _sql: String = "SELECT * FROM subtopics WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTopicId: Int = getColumnIndexOrThrow(_stmt, "topicId")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _result: SubtopicEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTopicId: String
          _tmpTopicId = _stmt.getText(_columnIndexOfTopicId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _result = SubtopicEntity(_tmpId,_tmpTopicId,_tmpName,_tmpIsCompleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private
      fun __fetchRelationshipsubtopicsAscomExampleDataSubtopicEntity(_connection: SQLiteConnection,
      _map: ArrayMap<String, MutableList<SubtopicEntity>>) {
    val __mapKeySet: Set<String> = _map.keys
    if (__mapKeySet.isEmpty()) {
      return
    }
    if (_map.size > 999) {
      recursiveFetchArrayMap(_map, true) { _tmpMap ->
        __fetchRelationshipsubtopicsAscomExampleDataSubtopicEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`topicId`,`name`,`isCompleted` FROM `subtopics` WHERE `topicId` IN (")
    val _inputSize: Int = __mapKeySet.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (_item: String in __mapKeySet) {
      _stmt.bindText(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "topicId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfTopicId: Int = 1
      val _columnIndexOfName: Int = 2
      val _columnIndexOfIsCompleted: Int = 3
      while (_stmt.step()) {
        val _tmpKey: String
        _tmpKey = _stmt.getText(_itemKeyIndex)
        val _tmpRelation: MutableList<SubtopicEntity>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
          val _item_1: SubtopicEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTopicId: String
          _tmpTopicId = _stmt.getText(_columnIndexOfTopicId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          _item_1 = SubtopicEntity(_tmpId,_tmpTopicId,_tmpName,_tmpIsCompleted)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  private
      fun __fetchRelationshiptopicsAscomExampleDataTopicWithSubtopics(_connection: SQLiteConnection,
      _map: ArrayMap<String, MutableList<TopicWithSubtopics>>) {
    val __mapKeySet: Set<String> = _map.keys
    if (__mapKeySet.isEmpty()) {
      return
    }
    if (_map.size > 999) {
      recursiveFetchArrayMap(_map, true) { _tmpMap ->
        __fetchRelationshiptopicsAscomExampleDataTopicWithSubtopics(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`subjectId`,`name`,`isPriority`,`isWeak`,`estimatedMinutes`,`isCompleted` FROM `topics` WHERE `subjectId` IN (")
    val _inputSize: Int = __mapKeySet.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (_item: String in __mapKeySet) {
      _stmt.bindText(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "subjectId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfSubjectId: Int = 1
      val _columnIndexOfName: Int = 2
      val _columnIndexOfIsPriority: Int = 3
      val _columnIndexOfIsWeak: Int = 4
      val _columnIndexOfEstimatedMinutes: Int = 5
      val _columnIndexOfIsCompleted: Int = 6
      val _collectionSubtopics: ArrayMap<String, MutableList<SubtopicEntity>> =
          ArrayMap<String, MutableList<SubtopicEntity>>()
      while (_stmt.step()) {
        val _tmpKey: String
        _tmpKey = _stmt.getText(_columnIndexOfId)
        if (!_collectionSubtopics.containsKey(_tmpKey)) {
          _collectionSubtopics.put(_tmpKey, mutableListOf())
        }
      }
      _stmt.reset()
      __fetchRelationshipsubtopicsAscomExampleDataSubtopicEntity(_connection, _collectionSubtopics)
      while (_stmt.step()) {
        val _tmpKey_1: String
        _tmpKey_1 = _stmt.getText(_itemKeyIndex)
        val _tmpRelation: MutableList<TopicWithSubtopics>? = _map.get(_tmpKey_1)
        if (_tmpRelation != null) {
          val _item_1: TopicWithSubtopics
          val _tmpTopic: TopicEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSubjectId: String
          _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsPriority: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsPriority).toInt()
          _tmpIsPriority = _tmp != 0
          val _tmpIsWeak: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsWeak).toInt()
          _tmpIsWeak = _tmp_1 != 0
          val _tmpEstimatedMinutes: Int
          _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          _tmpTopic =
              TopicEntity(_tmpId,_tmpSubjectId,_tmpName,_tmpIsPriority,_tmpIsWeak,_tmpEstimatedMinutes,_tmpIsCompleted)
          val _tmpSubtopicsCollection: MutableList<SubtopicEntity>
          val _tmpKey_2: String
          _tmpKey_2 = _stmt.getText(_columnIndexOfId)
          _tmpSubtopicsCollection = _collectionSubtopics.getValue(_tmpKey_2)
          _item_1 = TopicWithSubtopics(_tmpTopic,_tmpSubtopicsCollection)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
