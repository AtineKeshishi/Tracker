package com.akeshishi.tracker.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akeshishi.tracker.base.db.Action
import com.akeshishi.tracker.repository.MainRepository
import com.akeshishi.tracker.util.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository): ViewModel() {

    private val actionsSortedByDate = repository.getAllActionsSortedByDate()
    private val actionsSortedByDistance = repository.getAllActionsSortedByDistance()
    private val actionsSortedByCaloriesBurned = repository.getAllActionsSortedByCalories()
    private val actionsSortedByTimeInMillis = repository.getAllActionsSortedByTime()
    private val actionsSortedByAvgSpeed = repository.getAllActionsSortedBySpeed()

    val actions = MediatorLiveData<List<Action>>()

    var sortType = SortType.DATE

    init {
        actions.addSource(actionsSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let { actions.value = it }
            }
        }
        actions.addSource(actionsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { actions.value = it }
            }
        }
        actions.addSource(actionsSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { actions.value = it }
            }
        }
        actions.addSource(actionsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { actions.value = it }
            }
        }
        actions.addSource(actionsSortedByTimeInMillis) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { actions.value = it }
            }
        }
    }

    fun sortActions(sortType: SortType) = when(sortType) {
        SortType.DATE -> actionsSortedByDate.value?.let { actions.value = it }
        SortType.RUNNING_TIME -> actionsSortedByTimeInMillis.value?.let { actions.value = it }
        SortType.AVG_SPEED -> actionsSortedByAvgSpeed.value?.let { actions.value = it }
        SortType.DISTANCE -> actionsSortedByDistance.value?.let { actions.value = it }
        SortType.CALORIES_BURNED -> actionsSortedByCaloriesBurned.value?.let { actions.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertItem(action: Action) = viewModelScope.launch {
        repository.insertAction(action)
    }

    fun deleteItem(id: Int) = viewModelScope.launch {
        repository.deleteAction(id)
    }

    fun deleteAllItems() = viewModelScope.launch {
        repository.deleteAllActions()
    }

    fun getItem(id: Int) = repository.getAction(id)
}