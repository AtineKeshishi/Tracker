package com.akeshishi.tracker.ui

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.akeshishi.tracker.R
import com.akeshishi.tracker.base.db.Action
import com.akeshishi.tracker.base.extensions.makeGone
import com.akeshishi.tracker.base.extensions.makeVisible
import com.akeshishi.tracker.databinding.FragmentTrackingBinding
import com.akeshishi.tracker.services.TrackingService
import com.akeshishi.tracker.ui.viewmodels.MainViewModel
import com.akeshishi.tracker.util.Constants.ACTION_PAUSE_SERVICE
import com.akeshishi.tracker.util.Constants.ACTION_START_SERVICE
import com.akeshishi.tracker.util.Constants.ACTION_STOP_SERVICE
import com.akeshishi.tracker.util.Constants.DIALOG_TAG
import com.akeshishi.tracker.util.Constants.MAP_ZOOM
import com.akeshishi.tracker.util.Constants.POLYLINE_COLOR
import com.akeshishi.tracker.util.Constants.POLYLINE_WIDTH
import com.akeshishi.tracker.util.CustomDialogFragment
import com.akeshishi.tracker.util.Utility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewBinding: FragmentTrackingBinding
    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<MutableList<LatLng>>()
    private var currentTimeInMillis = 0L
    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @set:Inject
    var weight = 0f

    @set:Inject
    var name = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentTrackingBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            restoreDialogAfterRotation()
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        viewBinding.mapView.onCreate(savedInstanceState)
        viewBinding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }
        viewBinding.mapView.getMapAsync(this)

        setupToolbar()
        observerTracking()
        onButtonClick()

    }

    override fun onResume() {
        super.onResume()
        viewBinding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        viewBinding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewBinding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        viewBinding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewBinding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewBinding.mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        try {
            if (Utility.hasLocationPermissions(requireContext())) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ),
                                    MAP_ZOOM
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setupToolbar() {
        if (currentTimeInMillis > 0L)
            viewBinding.toolbar.txtCancel.makeVisible()

        viewBinding.toolbar.txtTitle.text = getString(R.string.lets_go, name)
    }

    private fun showCancelDialog() {
        CustomDialogFragment().apply {
            title = R.string.cancel_run_title
            message = R.string.cancel_run_message
            buttonFunction = { cancel() }
        }.show(parentFragmentManager, DIALOG_TAG)
    }

    private fun restoreDialogAfterRotation() {
        val dialog = parentFragmentManager.findFragmentByTag(DIALOG_TAG) as
                CustomDialogFragment?
        dialog?.let {
            it.title = R.string.cancel_run_title
            it.message = R.string.cancel_run_message
            it.buttonFunction = { cancel() }
        }
    }

    private fun cancel() {
        viewBinding.tvTimer.text = getString(R.string.total_time)
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.trackingFragmentToHomeFragment)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastCoordinate = pathPoints.last()[pathPoints.last().size - 2]
            val lastCoordinate = pathPoints.last().last()
            val polylineOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastCoordinate)
                .add(lastCoordinate)
            map?.addPolyline(polylineOption)
        }
    }

    private fun addAllPolyLines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), MAP_ZOOM)
            )
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis == 0L) {
            viewBinding.btnStart.text = getString(R.string.start)
            viewBinding.btnFinish.makeGone()
        } else if (!isTracking && currentTimeInMillis > 0L) {
            viewBinding.btnStart.text = getString(R.string.resume)
            viewBinding.btnFinish.makeVisible()
        } else if (isTracking) {
            viewBinding.toolbar.txtCancel.makeVisible()
            viewBinding.btnStart.text = getString(R.string.pause)
            viewBinding.btnFinish.makeGone()
        }
    }

    private fun start() {
        if (isTracking) {
            viewBinding.toolbar.txtCancel.makeVisible()
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_SERVICE)
        }
    }

    private fun observerTracking() {
        TrackingService.nowTracking.observe(viewLifecycleOwner, { updateTracking(it) })
        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeInMillis.observe(viewLifecycleOwner, {
            currentTimeInMillis = it
            val formattedTime = Utility.getStopWatchTime(currentTimeInMillis, true)
            viewBinding.tvTimer.text = formattedTime
        })
    }

    private fun seeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                viewBinding.mapView.width,
                viewBinding.mapView.height,
                (viewBinding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun finish() {
        map?.snapshot {
            var distance = 0
            for (polyline in pathPoints) {
                distance += Utility.calculateDistance(polyline).toInt()
            }
            val avgSpeed =
                round((distance / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distance / 1000f) * weight).toInt()
            val action =
                Action(it, dateTimeStamp, avgSpeed, distance, currentTimeInMillis, caloriesBurned)
            viewModel.insertItem(action)
            cancel()
        }
    }

    private fun onButtonClick() {
        viewBinding.toolbar.txtCancel.setOnClickListener { showCancelDialog() }
        viewBinding.btnStart.setOnClickListener { start() }
        viewBinding.btnFinish.setOnClickListener {
            seeWholeTrack()
            finish()
        }
    }
}