package com.akeshishi.tracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.akeshishi.tracker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(repository: MainRepository): ViewModel() {

    val totalTimeRun = repository.getTotalTime()
    val totalDistance = repository.getTotalDistance()
    val totalCaloriesBurned = repository.getTotalBurnedCalories()
    val totalAvgSpeed = repository.getTotalAvgSpeed()

    val actionsSortedByDate = repository.getAllActionsSortedByDate()
}