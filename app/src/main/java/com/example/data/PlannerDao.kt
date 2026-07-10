package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM daily_plans")
    fun getAllDailyPlansSync(): List<DailyPlanEntity>
    @Query("SELECT * FROM daily_plans WHERE dateString = :dateString")
    fun getPlan(dateString: String): Flow<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plans WHERE dateString = :dateString")
    suspend fun getPlanSync(dateString: String): DailyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlanEntity)

    @Query("UPDATE daily_plans SET isCompleted = :completed WHERE dateString = :dateString")
    suspend fun updatePlanCompletion(dateString: String, completed: Boolean)

    @androidx.room.Delete
    suspend fun deletePlan(plan: DailyPlanEntity)

    @androidx.room.Update
    suspend fun updatePlan(plan: DailyPlanEntity)

    @Query("SELECT * FROM daily_plans WHERE dateString >= :startDate AND dateString <= :endDate")
    fun getPlansInRange(startDate: String, endDate: String): Flow<List<DailyPlanEntity>>

}
