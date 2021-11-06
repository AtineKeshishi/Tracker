package com.akeshishi.tracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.akeshishi.tracker.R
import com.akeshishi.tracker.util.Constants.ACTION_PAUSE_SERVICE
import com.akeshishi.tracker.util.Constants.ACTION_START_SERVICE
import com.akeshishi.tracker.util.Constants.ACTION_STOP_SERVICE
import com.akeshishi.tracker.util.Constants.FASTEST_LOCATION_INTERVAL
import com.akeshishi.tracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.akeshishi.tracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.akeshishi.tracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.akeshishi.tracker.util.Constants.NOTIFICATION_ID
import com.akeshishi.tracker.util.Constants.TIMER_UPDATE_INTERVAL
import com.akeshishi.tracker.util.Utility
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstAction = true
    private var isServiceKilled = false
    private val timeInSeconds = MutableLiveData<Long>()
    private var isTimeEnabled = false
    private var totalTime = 0L
    private var startedTime = 0L
    private var lastSecondTimeStamp = 0L
    private var lapTime = 0L

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var initialNotification: NotificationCompat.Builder

    lateinit var currentNotification: NotificationCompat.Builder


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (nowTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    companion object {
        val nowTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
        val timeInMillis = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()
        initValues()
        currentNotification = initialNotification
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        nowTracking.observe(this, {
            updateLocation(it)
            updateNotificationStatus(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    if (isFirstAction) {
                        startForegroundService()
                        isFirstAction = false
                    } else
                        startTimer()
                }
                ACTION_PAUSE_SERVICE -> pauseService()
                ACTION_STOP_SERVICE -> killService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initValues() {
        nowTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeInSeconds.postValue(0L)
        timeInMillis.postValue(0L)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation(isTracking: Boolean) {
        if (isTracking) {
            if (Utility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startForegroundService() {
        startTimer()
        nowTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(notificationManager)

        startForeground(NOTIFICATION_ID, initialNotification.build())

        timeInSeconds.observe(this, Observer {
            if (!isServiceKilled) {
                val notification =
                    currentNotification.setContentText(Utility.getStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotificationStatus(isTracking: Boolean) {
        val notificationText = getString(if (isTracking) R.string.pause else R.string.resume)
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        currentNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotification, ArrayList<NotificationCompat.Action>())
        }

        if (!isServiceKilled) {
            currentNotification = initialNotification
                .addAction(R.drawable.ic_pause, notificationText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotification.build())
        }
    }

    private fun pauseService() {
        nowTracking.postValue(false)
        isTimeEnabled = false
    }

    private fun startTimer() {
        addEmptyPolyline()
        nowTracking.postValue(true)
        startedTime = System.currentTimeMillis()
        isTimeEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (nowTracking.value!!) {
                lapTime = System.currentTimeMillis() - startedTime
                timeInMillis.postValue(totalTime + lapTime)

                if (timeInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            totalTime += lapTime
        }
    }

    private fun killService() {
        isServiceKilled = true
        isFirstAction = true
        pauseService()
        initValues()
        stopForeground(true)
        stopSelf()
    }
}