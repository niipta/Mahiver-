package com.example.`data`

import androidx.collection.ArrayMap
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchArrayMap
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Float
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
public class MockDao_Impl(
  __db: RoomDatabase,
) : MockDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMockTestEntity: EntityInsertAdapter<MockTestEntity>

  private val __insertAdapterOfMockQuestionLogEntity: EntityInsertAdapter<MockQuestionLogEntity>

  private val __deleteAdapterOfMockTestEntity: EntityDeleteOrUpdateAdapter<MockTestEntity>

  private val __deleteAdapterOfMockQuestionLogEntity:
      EntityDeleteOrUpdateAdapter<MockQuestionLogEntity>

  private val __updateAdapterOfMockTestEntity: EntityDeleteOrUpdateAdapter<MockTestEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMockTestEntity = object : EntityInsertAdapter<MockTestEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `mock_tests` (`id`,`title`,`category`,`subjectId`,`subjectName`,`totalQuestions`,`durationMinutes`,`totalMarks`,`positiveMark`,`negativeMark`,`marksObtained`,`correctCount`,`wrongCount`,`unattemptedCount`,`actualDurationSeconds`,`percentile`,`rank`,`totalCandidates`,`attemptedAt`,`description`,`tags`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MockTestEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.category)
        val _tmpSubjectId: String? = entity.subjectId
        if (_tmpSubjectId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpSubjectId)
        }
        statement.bindText(5, entity.subjectName)
        statement.bindLong(6, entity.totalQuestions.toLong())
        statement.bindLong(7, entity.durationMinutes.toLong())
        statement.bindLong(8, entity.totalMarks.toLong())
        statement.bindDouble(9, entity.positiveMark.toDouble())
        statement.bindDouble(10, entity.negativeMark.toDouble())
        statement.bindDouble(11, entity.marksObtained.toDouble())
        statement.bindLong(12, entity.correctCount.toLong())
        statement.bindLong(13, entity.wrongCount.toLong())
        statement.bindLong(14, entity.unattemptedCount.toLong())
        statement.bindLong(15, entity.actualDurationSeconds.toLong())
        statement.bindDouble(16, entity.percentile.toDouble())
        statement.bindLong(17, entity.rank.toLong())
        statement.bindLong(18, entity.totalCandidates.toLong())
        statement.bindLong(19, entity.attemptedAt)
        statement.bindText(20, entity.description)
        statement.bindText(21, entity.tags)
      }
    }
    this.__insertAdapterOfMockQuestionLogEntity = object :
        EntityInsertAdapter<MockQuestionLogEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `mock_questions` (`id`,`mockTestId`,`questionNumber`,`subjectName`,`topicName`,`errorCategory`,`isCorrect`,`timeSpentSeconds`,`notes`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MockQuestionLogEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.mockTestId)
        statement.bindLong(3, entity.questionNumber.toLong())
        statement.bindText(4, entity.subjectName)
        statement.bindText(5, entity.topicName)
        statement.bindText(6, entity.errorCategory)
        val _tmp: Int = if (entity.isCorrect) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.timeSpentSeconds.toLong())
        statement.bindText(9, entity.notes)
      }
    }
    this.__deleteAdapterOfMockTestEntity = object : EntityDeleteOrUpdateAdapter<MockTestEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `mock_tests` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MockTestEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__deleteAdapterOfMockQuestionLogEntity = object :
        EntityDeleteOrUpdateAdapter<MockQuestionLogEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `mock_questions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MockQuestionLogEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfMockTestEntity = object : EntityDeleteOrUpdateAdapter<MockTestEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `mock_tests` SET `id` = ?,`title` = ?,`category` = ?,`subjectId` = ?,`subjectName` = ?,`totalQuestions` = ?,`durationMinutes` = ?,`totalMarks` = ?,`positiveMark` = ?,`negativeMark` = ?,`marksObtained` = ?,`correctCount` = ?,`wrongCount` = ?,`unattemptedCount` = ?,`actualDurationSeconds` = ?,`percentile` = ?,`rank` = ?,`totalCandidates` = ?,`attemptedAt` = ?,`description` = ?,`tags` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MockTestEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.category)
        val _tmpSubjectId: String? = entity.subjectId
        if (_tmpSubjectId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpSubjectId)
        }
        statement.bindText(5, entity.subjectName)
        statement.bindLong(6, entity.totalQuestions.toLong())
        statement.bindLong(7, entity.durationMinutes.toLong())
        statement.bindLong(8, entity.totalMarks.toLong())
        statement.bindDouble(9, entity.positiveMark.toDouble())
        statement.bindDouble(10, entity.negativeMark.toDouble())
        statement.bindDouble(11, entity.marksObtained.toDouble())
        statement.bindLong(12, entity.correctCount.toLong())
        statement.bindLong(13, entity.wrongCount.toLong())
        statement.bindLong(14, entity.unattemptedCount.toLong())
        statement.bindLong(15, entity.actualDurationSeconds.toLong())
        statement.bindDouble(16, entity.percentile.toDouble())
        statement.bindLong(17, entity.rank.toLong())
        statement.bindLong(18, entity.totalCandidates.toLong())
        statement.bindLong(19, entity.attemptedAt)
        statement.bindText(20, entity.description)
        statement.bindText(21, entity.tags)
        statement.bindText(22, entity.id)
      }
    }
  }

  public override suspend fun insertMockTest(mockTest: MockTestEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMockTestEntity.insert(_connection, mockTest)
  }

  public override suspend fun insertQuestionLog(question: MockQuestionLogEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMockQuestionLogEntity.insert(_connection, question)
  }

  public override suspend fun insertQuestionLogs(questions: List<MockQuestionLogEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMockQuestionLogEntity.insert(_connection, questions)
  }

  public override suspend fun deleteMockTest(mockTest: MockTestEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMockTestEntity.handle(_connection, mockTest)
  }

  public override suspend fun deleteQuestionLog(question: MockQuestionLogEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMockQuestionLogEntity.handle(_connection, question)
  }

  public override suspend fun updateMockTest(mockTest: MockTestEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfMockTestEntity.handle(_connection, mockTest)
  }

  public override fun getAllMockTests(): Flow<List<MockTestWithQuestions>> {
    val _sql: String = "SELECT * FROM mock_tests ORDER BY attemptedAt DESC"
    return createFlow(__db, true, arrayOf("mock_questions", "mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTotalQuestions: Int = getColumnIndexOrThrow(_stmt, "totalQuestions")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfTotalMarks: Int = getColumnIndexOrThrow(_stmt, "totalMarks")
        val _columnIndexOfPositiveMark: Int = getColumnIndexOrThrow(_stmt, "positiveMark")
        val _columnIndexOfNegativeMark: Int = getColumnIndexOrThrow(_stmt, "negativeMark")
        val _columnIndexOfMarksObtained: Int = getColumnIndexOrThrow(_stmt, "marksObtained")
        val _columnIndexOfCorrectCount: Int = getColumnIndexOrThrow(_stmt, "correctCount")
        val _columnIndexOfWrongCount: Int = getColumnIndexOrThrow(_stmt, "wrongCount")
        val _columnIndexOfUnattemptedCount: Int = getColumnIndexOrThrow(_stmt, "unattemptedCount")
        val _columnIndexOfActualDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "actualDurationSeconds")
        val _columnIndexOfPercentile: Int = getColumnIndexOrThrow(_stmt, "percentile")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfTotalCandidates: Int = getColumnIndexOrThrow(_stmt, "totalCandidates")
        val _columnIndexOfAttemptedAt: Int = getColumnIndexOrThrow(_stmt, "attemptedAt")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _collectionQuestions: ArrayMap<String, MutableList<MockQuestionLogEntity>> =
            ArrayMap<String, MutableList<MockQuestionLogEntity>>()
        while (_stmt.step()) {
          val _tmpKey: String
          _tmpKey = _stmt.getText(_columnIndexOfId)
          if (!_collectionQuestions.containsKey(_tmpKey)) {
            _collectionQuestions.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipmockQuestionsAscomExampleDataMockQuestionLogEntity(_connection,
            _collectionQuestions)
        val _result: MutableList<MockTestWithQuestions> = mutableListOf()
        while (_stmt.step()) {
          val _item: MockTestWithQuestions
          val _tmpMockTest: MockTestEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpSubjectId: String?
          if (_stmt.isNull(_columnIndexOfSubjectId)) {
            _tmpSubjectId = null
          } else {
            _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          }
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTotalQuestions: Int
          _tmpTotalQuestions = _stmt.getLong(_columnIndexOfTotalQuestions).toInt()
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpTotalMarks: Int
          _tmpTotalMarks = _stmt.getLong(_columnIndexOfTotalMarks).toInt()
          val _tmpPositiveMark: Float
          _tmpPositiveMark = _stmt.getDouble(_columnIndexOfPositiveMark).toFloat()
          val _tmpNegativeMark: Float
          _tmpNegativeMark = _stmt.getDouble(_columnIndexOfNegativeMark).toFloat()
          val _tmpMarksObtained: Float
          _tmpMarksObtained = _stmt.getDouble(_columnIndexOfMarksObtained).toFloat()
          val _tmpCorrectCount: Int
          _tmpCorrectCount = _stmt.getLong(_columnIndexOfCorrectCount).toInt()
          val _tmpWrongCount: Int
          _tmpWrongCount = _stmt.getLong(_columnIndexOfWrongCount).toInt()
          val _tmpUnattemptedCount: Int
          _tmpUnattemptedCount = _stmt.getLong(_columnIndexOfUnattemptedCount).toInt()
          val _tmpActualDurationSeconds: Int
          _tmpActualDurationSeconds = _stmt.getLong(_columnIndexOfActualDurationSeconds).toInt()
          val _tmpPercentile: Float
          _tmpPercentile = _stmt.getDouble(_columnIndexOfPercentile).toFloat()
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpTotalCandidates: Int
          _tmpTotalCandidates = _stmt.getLong(_columnIndexOfTotalCandidates).toInt()
          val _tmpAttemptedAt: Long
          _tmpAttemptedAt = _stmt.getLong(_columnIndexOfAttemptedAt)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          _tmpMockTest =
              MockTestEntity(_tmpId,_tmpTitle,_tmpCategory,_tmpSubjectId,_tmpSubjectName,_tmpTotalQuestions,_tmpDurationMinutes,_tmpTotalMarks,_tmpPositiveMark,_tmpNegativeMark,_tmpMarksObtained,_tmpCorrectCount,_tmpWrongCount,_tmpUnattemptedCount,_tmpActualDurationSeconds,_tmpPercentile,_tmpRank,_tmpTotalCandidates,_tmpAttemptedAt,_tmpDescription,_tmpTags)
          val _tmpQuestionsCollection: MutableList<MockQuestionLogEntity>
          val _tmpKey_1: String
          _tmpKey_1 = _stmt.getText(_columnIndexOfId)
          _tmpQuestionsCollection = _collectionQuestions.getValue(_tmpKey_1)
          _item = MockTestWithQuestions(_tmpMockTest,_tmpQuestionsCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMockTestById(id: String): MockTestWithQuestions? {
    val _sql: String = "SELECT * FROM mock_tests WHERE id = ?"
    return performSuspending(__db, true, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTotalQuestions: Int = getColumnIndexOrThrow(_stmt, "totalQuestions")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfTotalMarks: Int = getColumnIndexOrThrow(_stmt, "totalMarks")
        val _columnIndexOfPositiveMark: Int = getColumnIndexOrThrow(_stmt, "positiveMark")
        val _columnIndexOfNegativeMark: Int = getColumnIndexOrThrow(_stmt, "negativeMark")
        val _columnIndexOfMarksObtained: Int = getColumnIndexOrThrow(_stmt, "marksObtained")
        val _columnIndexOfCorrectCount: Int = getColumnIndexOrThrow(_stmt, "correctCount")
        val _columnIndexOfWrongCount: Int = getColumnIndexOrThrow(_stmt, "wrongCount")
        val _columnIndexOfUnattemptedCount: Int = getColumnIndexOrThrow(_stmt, "unattemptedCount")
        val _columnIndexOfActualDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "actualDurationSeconds")
        val _columnIndexOfPercentile: Int = getColumnIndexOrThrow(_stmt, "percentile")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfTotalCandidates: Int = getColumnIndexOrThrow(_stmt, "totalCandidates")
        val _columnIndexOfAttemptedAt: Int = getColumnIndexOrThrow(_stmt, "attemptedAt")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _collectionQuestions: ArrayMap<String, MutableList<MockQuestionLogEntity>> =
            ArrayMap<String, MutableList<MockQuestionLogEntity>>()
        while (_stmt.step()) {
          val _tmpKey: String
          _tmpKey = _stmt.getText(_columnIndexOfId)
          if (!_collectionQuestions.containsKey(_tmpKey)) {
            _collectionQuestions.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipmockQuestionsAscomExampleDataMockQuestionLogEntity(_connection,
            _collectionQuestions)
        val _result: MockTestWithQuestions?
        if (_stmt.step()) {
          val _tmpMockTest: MockTestEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpSubjectId: String?
          if (_stmt.isNull(_columnIndexOfSubjectId)) {
            _tmpSubjectId = null
          } else {
            _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          }
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTotalQuestions: Int
          _tmpTotalQuestions = _stmt.getLong(_columnIndexOfTotalQuestions).toInt()
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpTotalMarks: Int
          _tmpTotalMarks = _stmt.getLong(_columnIndexOfTotalMarks).toInt()
          val _tmpPositiveMark: Float
          _tmpPositiveMark = _stmt.getDouble(_columnIndexOfPositiveMark).toFloat()
          val _tmpNegativeMark: Float
          _tmpNegativeMark = _stmt.getDouble(_columnIndexOfNegativeMark).toFloat()
          val _tmpMarksObtained: Float
          _tmpMarksObtained = _stmt.getDouble(_columnIndexOfMarksObtained).toFloat()
          val _tmpCorrectCount: Int
          _tmpCorrectCount = _stmt.getLong(_columnIndexOfCorrectCount).toInt()
          val _tmpWrongCount: Int
          _tmpWrongCount = _stmt.getLong(_columnIndexOfWrongCount).toInt()
          val _tmpUnattemptedCount: Int
          _tmpUnattemptedCount = _stmt.getLong(_columnIndexOfUnattemptedCount).toInt()
          val _tmpActualDurationSeconds: Int
          _tmpActualDurationSeconds = _stmt.getLong(_columnIndexOfActualDurationSeconds).toInt()
          val _tmpPercentile: Float
          _tmpPercentile = _stmt.getDouble(_columnIndexOfPercentile).toFloat()
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpTotalCandidates: Int
          _tmpTotalCandidates = _stmt.getLong(_columnIndexOfTotalCandidates).toInt()
          val _tmpAttemptedAt: Long
          _tmpAttemptedAt = _stmt.getLong(_columnIndexOfAttemptedAt)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          _tmpMockTest =
              MockTestEntity(_tmpId,_tmpTitle,_tmpCategory,_tmpSubjectId,_tmpSubjectName,_tmpTotalQuestions,_tmpDurationMinutes,_tmpTotalMarks,_tmpPositiveMark,_tmpNegativeMark,_tmpMarksObtained,_tmpCorrectCount,_tmpWrongCount,_tmpUnattemptedCount,_tmpActualDurationSeconds,_tmpPercentile,_tmpRank,_tmpTotalCandidates,_tmpAttemptedAt,_tmpDescription,_tmpTags)
          val _tmpQuestionsCollection: MutableList<MockQuestionLogEntity>
          val _tmpKey_1: String
          _tmpKey_1 = _stmt.getText(_columnIndexOfId)
          _tmpQuestionsCollection = _collectionQuestions.getValue(_tmpKey_1)
          _result = MockTestWithQuestions(_tmpMockTest,_tmpQuestionsCollection)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getRawMockTest(id: String): MockTestEntity? {
    val _sql: String = "SELECT * FROM mock_tests WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfSubjectId: Int = getColumnIndexOrThrow(_stmt, "subjectId")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTotalQuestions: Int = getColumnIndexOrThrow(_stmt, "totalQuestions")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "durationMinutes")
        val _columnIndexOfTotalMarks: Int = getColumnIndexOrThrow(_stmt, "totalMarks")
        val _columnIndexOfPositiveMark: Int = getColumnIndexOrThrow(_stmt, "positiveMark")
        val _columnIndexOfNegativeMark: Int = getColumnIndexOrThrow(_stmt, "negativeMark")
        val _columnIndexOfMarksObtained: Int = getColumnIndexOrThrow(_stmt, "marksObtained")
        val _columnIndexOfCorrectCount: Int = getColumnIndexOrThrow(_stmt, "correctCount")
        val _columnIndexOfWrongCount: Int = getColumnIndexOrThrow(_stmt, "wrongCount")
        val _columnIndexOfUnattemptedCount: Int = getColumnIndexOrThrow(_stmt, "unattemptedCount")
        val _columnIndexOfActualDurationSeconds: Int = getColumnIndexOrThrow(_stmt,
            "actualDurationSeconds")
        val _columnIndexOfPercentile: Int = getColumnIndexOrThrow(_stmt, "percentile")
        val _columnIndexOfRank: Int = getColumnIndexOrThrow(_stmt, "rank")
        val _columnIndexOfTotalCandidates: Int = getColumnIndexOrThrow(_stmt, "totalCandidates")
        val _columnIndexOfAttemptedAt: Int = getColumnIndexOrThrow(_stmt, "attemptedAt")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _result: MockTestEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpSubjectId: String?
          if (_stmt.isNull(_columnIndexOfSubjectId)) {
            _tmpSubjectId = null
          } else {
            _tmpSubjectId = _stmt.getText(_columnIndexOfSubjectId)
          }
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTotalQuestions: Int
          _tmpTotalQuestions = _stmt.getLong(_columnIndexOfTotalQuestions).toInt()
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpTotalMarks: Int
          _tmpTotalMarks = _stmt.getLong(_columnIndexOfTotalMarks).toInt()
          val _tmpPositiveMark: Float
          _tmpPositiveMark = _stmt.getDouble(_columnIndexOfPositiveMark).toFloat()
          val _tmpNegativeMark: Float
          _tmpNegativeMark = _stmt.getDouble(_columnIndexOfNegativeMark).toFloat()
          val _tmpMarksObtained: Float
          _tmpMarksObtained = _stmt.getDouble(_columnIndexOfMarksObtained).toFloat()
          val _tmpCorrectCount: Int
          _tmpCorrectCount = _stmt.getLong(_columnIndexOfCorrectCount).toInt()
          val _tmpWrongCount: Int
          _tmpWrongCount = _stmt.getLong(_columnIndexOfWrongCount).toInt()
          val _tmpUnattemptedCount: Int
          _tmpUnattemptedCount = _stmt.getLong(_columnIndexOfUnattemptedCount).toInt()
          val _tmpActualDurationSeconds: Int
          _tmpActualDurationSeconds = _stmt.getLong(_columnIndexOfActualDurationSeconds).toInt()
          val _tmpPercentile: Float
          _tmpPercentile = _stmt.getDouble(_columnIndexOfPercentile).toFloat()
          val _tmpRank: Int
          _tmpRank = _stmt.getLong(_columnIndexOfRank).toInt()
          val _tmpTotalCandidates: Int
          _tmpTotalCandidates = _stmt.getLong(_columnIndexOfTotalCandidates).toInt()
          val _tmpAttemptedAt: Long
          _tmpAttemptedAt = _stmt.getLong(_columnIndexOfAttemptedAt)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          _result =
              MockTestEntity(_tmpId,_tmpTitle,_tmpCategory,_tmpSubjectId,_tmpSubjectName,_tmpTotalQuestions,_tmpDurationMinutes,_tmpTotalMarks,_tmpPositiveMark,_tmpNegativeMark,_tmpMarksObtained,_tmpCorrectCount,_tmpWrongCount,_tmpUnattemptedCount,_tmpActualDurationSeconds,_tmpPercentile,_tmpRank,_tmpTotalCandidates,_tmpAttemptedAt,_tmpDescription,_tmpTags)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMockTestCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM mock_tests"
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
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

  public override fun getTotalQuestionCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM mock_questions"
    return createFlow(__db, false, arrayOf("mock_questions")) { _connection ->
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

  public override suspend fun getQuestionsForTestSync(mockTestId: String):
      List<MockQuestionLogEntity> {
    val _sql: String =
        "SELECT * FROM mock_questions WHERE mockTestId = ? ORDER BY questionNumber ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, mockTestId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMockTestId: Int = getColumnIndexOrThrow(_stmt, "mockTestId")
        val _columnIndexOfQuestionNumber: Int = getColumnIndexOrThrow(_stmt, "questionNumber")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTopicName: Int = getColumnIndexOrThrow(_stmt, "topicName")
        val _columnIndexOfErrorCategory: Int = getColumnIndexOrThrow(_stmt, "errorCategory")
        val _columnIndexOfIsCorrect: Int = getColumnIndexOrThrow(_stmt, "isCorrect")
        val _columnIndexOfTimeSpentSeconds: Int = getColumnIndexOrThrow(_stmt, "timeSpentSeconds")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<MockQuestionLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MockQuestionLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMockTestId: String
          _tmpMockTestId = _stmt.getText(_columnIndexOfMockTestId)
          val _tmpQuestionNumber: Int
          _tmpQuestionNumber = _stmt.getLong(_columnIndexOfQuestionNumber).toInt()
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String
          _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          val _tmpErrorCategory: String
          _tmpErrorCategory = _stmt.getText(_columnIndexOfErrorCategory)
          val _tmpIsCorrect: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCorrect).toInt()
          _tmpIsCorrect = _tmp != 0
          val _tmpTimeSpentSeconds: Int
          _tmpTimeSpentSeconds = _stmt.getLong(_columnIndexOfTimeSpentSeconds).toInt()
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item =
              MockQuestionLogEntity(_tmpId,_tmpMockTestId,_tmpQuestionNumber,_tmpSubjectName,_tmpTopicName,_tmpErrorCategory,_tmpIsCorrect,_tmpTimeSpentSeconds,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getQuestionsForTest(mockTestId: String): Flow<List<MockQuestionLogEntity>> {
    val _sql: String =
        "SELECT * FROM mock_questions WHERE mockTestId = ? ORDER BY questionNumber ASC"
    return createFlow(__db, false, arrayOf("mock_questions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, mockTestId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMockTestId: Int = getColumnIndexOrThrow(_stmt, "mockTestId")
        val _columnIndexOfQuestionNumber: Int = getColumnIndexOrThrow(_stmt, "questionNumber")
        val _columnIndexOfSubjectName: Int = getColumnIndexOrThrow(_stmt, "subjectName")
        val _columnIndexOfTopicName: Int = getColumnIndexOrThrow(_stmt, "topicName")
        val _columnIndexOfErrorCategory: Int = getColumnIndexOrThrow(_stmt, "errorCategory")
        val _columnIndexOfIsCorrect: Int = getColumnIndexOrThrow(_stmt, "isCorrect")
        val _columnIndexOfTimeSpentSeconds: Int = getColumnIndexOrThrow(_stmt, "timeSpentSeconds")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<MockQuestionLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MockQuestionLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMockTestId: String
          _tmpMockTestId = _stmt.getText(_columnIndexOfMockTestId)
          val _tmpQuestionNumber: Int
          _tmpQuestionNumber = _stmt.getLong(_columnIndexOfQuestionNumber).toInt()
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String
          _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          val _tmpErrorCategory: String
          _tmpErrorCategory = _stmt.getText(_columnIndexOfErrorCategory)
          val _tmpIsCorrect: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCorrect).toInt()
          _tmpIsCorrect = _tmp != 0
          val _tmpTimeSpentSeconds: Int
          _tmpTimeSpentSeconds = _stmt.getLong(_columnIndexOfTimeSpentSeconds).toInt()
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item =
              MockQuestionLogEntity(_tmpId,_tmpMockTestId,_tmpQuestionNumber,_tmpSubjectName,_tmpTopicName,_tmpErrorCategory,_tmpIsCorrect,_tmpTimeSpentSeconds,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getErrorCategoryCountsSync(): List<CategoryCount> {
    val _sql: String = """
        |
        |        SELECT errorCategory, COUNT(*) as count
        |        FROM mock_questions
        |        GROUP BY errorCategory
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfErrorCategory: Int = 0
        val _columnIndexOfCount: Int = 1
        val _result: MutableList<CategoryCount> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryCount
          val _tmpErrorCategory: String
          _tmpErrorCategory = _stmt.getText(_columnIndexOfErrorCategory)
          val _tmpCount: Int
          _tmpCount = _stmt.getLong(_columnIndexOfCount).toInt()
          _item = CategoryCount(_tmpErrorCategory,_tmpCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTopicAggregationSync(): List<TopicAggregationRow> {
    val _sql: String = """
        |
        |        SELECT subjectName, topicName,
        |               COUNT(*) as totalQuestions,
        |               SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correctCount,
        |               SUM(CASE WHEN errorCategory = 'WRONG' OR (isCorrect = 0 AND errorCategory != 'UNATTEMPTED') THEN 1 ELSE 0 END) as wrongCount,
        |               SUM(CASE WHEN errorCategory = 'UNATTEMPTED' THEN 1 ELSE 0 END) as unattemptedCount
        |        FROM mock_questions
        |        WHERE topicName != ''
        |        GROUP BY subjectName, topicName
        |        ORDER BY totalQuestions DESC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSubjectName: Int = 0
        val _columnIndexOfTopicName: Int = 1
        val _columnIndexOfTotalQuestions: Int = 2
        val _columnIndexOfCorrectCount: Int = 3
        val _columnIndexOfWrongCount: Int = 4
        val _columnIndexOfUnattemptedCount: Int = 5
        val _result: MutableList<TopicAggregationRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: TopicAggregationRow
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String
          _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          val _tmpTotalQuestions: Int
          _tmpTotalQuestions = _stmt.getLong(_columnIndexOfTotalQuestions).toInt()
          val _tmpCorrectCount: Int
          _tmpCorrectCount = _stmt.getLong(_columnIndexOfCorrectCount).toInt()
          val _tmpWrongCount: Int
          _tmpWrongCount = _stmt.getLong(_columnIndexOfWrongCount).toInt()
          val _tmpUnattemptedCount: Int
          _tmpUnattemptedCount = _stmt.getLong(_columnIndexOfUnattemptedCount).toInt()
          _item =
              TopicAggregationRow(_tmpSubjectName,_tmpTopicName,_tmpTotalQuestions,_tmpCorrectCount,_tmpWrongCount,_tmpUnattemptedCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSubjectAggregationSync(): List<SubjectAggregationRow> {
    val _sql: String = """
        |
        |        SELECT subjectName,
        |               COUNT(*) as totalQuestions,
        |               SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correctCount,
        |               SUM(CASE WHEN isCorrect = 0 AND errorCategory != 'UNATTEMPTED' THEN 1 ELSE 0 END) as wrongCount,
        |               SUM(CASE WHEN errorCategory = 'UNATTEMPTED' THEN 1 ELSE 0 END) as unattemptedCount
        |        FROM mock_questions
        |        WHERE subjectName != ''
        |        GROUP BY subjectName
        |        ORDER BY totalQuestions DESC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSubjectName: Int = 0
        val _columnIndexOfTotalQuestions: Int = 1
        val _columnIndexOfCorrectCount: Int = 2
        val _columnIndexOfWrongCount: Int = 3
        val _columnIndexOfUnattemptedCount: Int = 4
        val _result: MutableList<SubjectAggregationRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubjectAggregationRow
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTotalQuestions: Int
          _tmpTotalQuestions = _stmt.getLong(_columnIndexOfTotalQuestions).toInt()
          val _tmpCorrectCount: Int
          _tmpCorrectCount = _stmt.getLong(_columnIndexOfCorrectCount).toInt()
          val _tmpWrongCount: Int
          _tmpWrongCount = _stmt.getLong(_columnIndexOfWrongCount).toInt()
          val _tmpUnattemptedCount: Int
          _tmpUnattemptedCount = _stmt.getLong(_columnIndexOfUnattemptedCount).toInt()
          _item =
              SubjectAggregationRow(_tmpSubjectName,_tmpTotalQuestions,_tmpCorrectCount,_tmpWrongCount,_tmpUnattemptedCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAveragePercentage(): Flow<Float?> {
    val _sql: String = """
        |
        |        SELECT AVG(CASE WHEN totalMarks > 0 THEN (marksObtained * 100.0 / totalMarks) ELSE 0 END)
        |        FROM mock_tests
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getBestPercentage(): Flow<Float?> {
    val _sql: String = """
        |
        |        SELECT MAX(CASE WHEN totalMarks > 0 THEN (marksObtained * 100.0 / totalMarks) ELSE 0 END)
        |        FROM mock_tests
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAveragePercentile(): Flow<Float?> {
    val _sql: String = "SELECT AVG(percentile) FROM mock_tests WHERE percentile > 0"
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getBestPercentile(): Flow<Float?> {
    val _sql: String = "SELECT MAX(percentile) FROM mock_tests WHERE percentile > 0"
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Float?
        if (_stmt.step()) {
          val _tmp: Float?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0).toFloat()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTotalMockTimeSeconds(): Flow<Int?> {
    val _sql: String = "SELECT SUM(actualDurationSeconds) FROM mock_tests"
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int?
        if (_stmt.step()) {
          val _tmp: Int?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0).toInt()
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getDistinctSubjects(): Flow<List<String>> {
    val _sql: String =
        "SELECT DISTINCT subjectName FROM mock_tests WHERE subjectName != '' ORDER BY subjectName ASC"
    return createFlow(__db, false, arrayOf("mock_tests")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMockTestById(id: String) {
    val _sql: String = "DELETE FROM mock_tests WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteQuestionsForTest(mockTestId: String) {
    val _sql: String = "DELETE FROM mock_questions WHERE mockTestId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, mockTestId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private
      fun __fetchRelationshipmockQuestionsAscomExampleDataMockQuestionLogEntity(_connection: SQLiteConnection,
      _map: ArrayMap<String, MutableList<MockQuestionLogEntity>>) {
    val __mapKeySet: Set<String> = _map.keys
    if (__mapKeySet.isEmpty()) {
      return
    }
    if (_map.size > 999) {
      recursiveFetchArrayMap(_map, true) { _tmpMap ->
        __fetchRelationshipmockQuestionsAscomExampleDataMockQuestionLogEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`mockTestId`,`questionNumber`,`subjectName`,`topicName`,`errorCategory`,`isCorrect`,`timeSpentSeconds`,`notes` FROM `mock_questions` WHERE `mockTestId` IN (")
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
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "mockTestId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfMockTestId: Int = 1
      val _columnIndexOfQuestionNumber: Int = 2
      val _columnIndexOfSubjectName: Int = 3
      val _columnIndexOfTopicName: Int = 4
      val _columnIndexOfErrorCategory: Int = 5
      val _columnIndexOfIsCorrect: Int = 6
      val _columnIndexOfTimeSpentSeconds: Int = 7
      val _columnIndexOfNotes: Int = 8
      while (_stmt.step()) {
        val _tmpKey: String
        _tmpKey = _stmt.getText(_itemKeyIndex)
        val _tmpRelation: MutableList<MockQuestionLogEntity>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
          val _item_1: MockQuestionLogEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMockTestId: String
          _tmpMockTestId = _stmt.getText(_columnIndexOfMockTestId)
          val _tmpQuestionNumber: Int
          _tmpQuestionNumber = _stmt.getLong(_columnIndexOfQuestionNumber).toInt()
          val _tmpSubjectName: String
          _tmpSubjectName = _stmt.getText(_columnIndexOfSubjectName)
          val _tmpTopicName: String
          _tmpTopicName = _stmt.getText(_columnIndexOfTopicName)
          val _tmpErrorCategory: String
          _tmpErrorCategory = _stmt.getText(_columnIndexOfErrorCategory)
          val _tmpIsCorrect: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCorrect).toInt()
          _tmpIsCorrect = _tmp != 0
          val _tmpTimeSpentSeconds: Int
          _tmpTimeSpentSeconds = _stmt.getLong(_columnIndexOfTimeSpentSeconds).toInt()
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item_1 =
              MockQuestionLogEntity(_tmpId,_tmpMockTestId,_tmpQuestionNumber,_tmpSubjectName,_tmpTopicName,_tmpErrorCategory,_tmpIsCorrect,_tmpTimeSpentSeconds,_tmpNotes)
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
