package com.example.data

import com.example.data.sync.SyncDao
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.room.withTransaction
class PlannerRepository(private val db: AppDatabase, private val syncDao: SyncDao) {
    private val plannerDao = db.plannerDao()
    fun getPlanForDate(dateString: String): Flow<DailyPlanEntity?> {
        return plannerDao.getPlan(dateString)
    }

    fun getPlansInRange(startDate: String, endDate: String): Flow<List<DailyPlanEntity>> {
        return plannerDao.getPlansInRange(startDate, endDate)
    }

    suspend fun insertPlan(plan: DailyPlanEntity) {
        db.withTransaction {
        plannerDao.insertPlan(plan)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "INSERT", entityType = "PLANNER", entityId = plan.dateString))
    }
    }
    
    suspend fun updatePlanCompletion(dateString: String, completed: Boolean) {
        db.withTransaction {
        plannerDao.updatePlanCompletion(dateString, completed)
        syncDao.insertSyncTask(SyncQueueEntity(operationType = "UPDATE", entityType = "PLANNER", entityId = dateString))
    }
    }
    
    companion object {
        fun getDateString(millis: Long = System.currentTimeMillis()): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(Date(millis))
        }
    }
}
