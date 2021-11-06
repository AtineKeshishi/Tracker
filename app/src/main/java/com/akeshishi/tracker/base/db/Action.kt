package com.akeshishi.tracker.base.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_table")
data class Action(
    var img: Bitmap? = null,
    var timeStamp: Long = 0L,
    var avgSpeed: Float = 0f,
    var distance: Int = 0,
    var timeInMillis: Long = 0L,
    var burnedCalories: Int = 0
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
