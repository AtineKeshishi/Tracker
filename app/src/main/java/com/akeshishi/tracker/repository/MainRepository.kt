package com.akeshishi.tracker.repository

import com.akeshishi.tracker.base.db.Action
import com.akeshishi.tracker.base.db.ActionDao
import javax.inject.Inject

class MainRepository @Inject constructor(private val actionDao: ActionDao) {

    suspend fun insertAction(action: Action) = actionDao.insertAction(action)

    suspend fun deleteAction(itemId: Int) = actionDao.deleteAction(itemId)

    suspend fun deleteAllActions() = actionDao.deleteAllActions()

    fun getAction(itemId: Int) = actionDao.getAction(itemId)

    fun getAllActionsSortedByDate() = actionDao.getAllActionsSortedByDate()

    fun getAllActionsSortedBySpeed() = actionDao.getAllActionsSortedBySpeed()

    fun getAllActionsSortedByDistance() = actionDao.getAllActionsSortedByDistance()

    fun getAllActionsSortedByTime() = actionDao.getAllActionsSortedByTime()

    fun getAllActionsSortedByCalories() = actionDao.getAllActionsSortedByCalories()

    fun getTotalTime() = actionDao.getTotalTime()

    fun getTotalBurnedCalories() = actionDao.getTotalBurnedCalories()

    fun getTotalDistance() = actionDao.getTotalDistance()

    fun getTotalAvgSpeed() = actionDao.getTotalAvgSpeed()
}