package com.akeshishi.tracker.base.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: Action)

    @Query("DELETE FROM action_table WHERE id = :itemId")
    suspend fun deleteAction(itemId: Int)

    @Query("DELETE FROM action_table")
    suspend fun deleteAllActions()

    @Query("SELECT * FROM action_table WHERE id = :itemId")
    fun getAction(itemId: Int): LiveData<Action>

    @Query("SELECT * FROM action_table ORDER BY timeStamp DESC")
    fun getAllActionsSortedByDate(): LiveData<List<Action>>

    @Query("SELECT * FROM action_table ORDER BY avgSpeed DESC")
    fun getAllActionsSortedBySpeed(): LiveData<List<Action>>

    @Query("SELECT * FROM action_table ORDER BY distance DESC")
    fun getAllActionsSortedByDistance(): LiveData<List<Action>>

    @Query("SELECT * FROM action_table ORDER BY timeInMillis DESC")
    fun getAllActionsSortedByTime(): LiveData<List<Action>>

    @Query("SELECT * FROM action_table ORDER BY burnedCalories DESC")
    fun getAllActionsSortedByCalories(): LiveData<List<Action>>

    @Query("SELECT SUM(timeInMillis) FROM action_table")
    fun getTotalTime(): LiveData<Long>

    @Query("SELECT SUM(burnedCalories) FROM action_table")
    fun getTotalBurnedCalories(): LiveData<Int>

    @Query("SELECT SUM(distance) FROM action_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT AVG(avgSpeed) FROM action_table")
    fun getTotalAvgSpeed(): LiveData<Float>
}