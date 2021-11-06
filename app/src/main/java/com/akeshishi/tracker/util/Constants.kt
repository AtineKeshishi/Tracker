package com.akeshishi.tracker.util

import android.graphics.Color

object Constants {

    const val DATABASE_NAME = "action_database"
    const val LOCATION_PERMISSION_REQUEST_CODE = 0

    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f

    const val MAP_ZOOM = 15f

    const val TIMER_UPDATE_INTERVAL = 50L

    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val FIRST_TIME_LOGIN = "FIRST_TIME_LOGIN"

    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val KEY_HEIGHT = "KEY_HEIGHT"
    const val KEY_ITEM_ID = "KEY_ITEM_ID"

    const val DIALOG_TAG = "dialog_tag"
    const val ACTION_DIALOG_TAG = "action_dialog_tag"
}