package com.akeshishi.tracker.util

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.akeshishi.tracker.base.db.Action
import com.google.android.gms.maps.model.LatLng
import pub.devrel.easypermissions.EasyPermissions
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utility {

    fun hasLocationPermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun getStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var millis = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)

        if (!includeMillis) {
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds"
        }
        millis -= TimeUnit.SECONDS.toMillis(seconds)
        millis /= 10    // Because we want 2 digits for our milliseconds not 3 digits.
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (millis < 10) "0" else ""}$millis"
    }

    fun calculateDistance(polyline: MutableList<LatLng>): Float {
        var distance = 0f
        for (index in 0..polyline.size - 2) {
            val firstPosition = polyline[index]
            val secondPosition = polyline[index + 1]

            val result = FloatArray(1)

            Location.distanceBetween(
                firstPosition.latitude,
                firstPosition.longitude,
                secondPosition.latitude,
                secondPosition.longitude,
                result
            )
            distance += result[0]
        }
        return distance
    }

    fun getShareData(action: Action): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = action.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val string = StringBuilder()
        string.append(
            "My results for ${dateFormat.format(calendar.time)} are\n" +
                    "time: ${getStopWatchTime(action.timeInMillis)}\n" +
                    "distance: ${action.distance / 1000f} km\n" +
                    "avg speed: ${action.avgSpeed} km/h\n" +
                    "burned calories: ${action.burnedCalories}"
        )
        return string.toString()
    }
}
