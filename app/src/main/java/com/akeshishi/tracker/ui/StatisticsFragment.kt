package com.akeshishi.tracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.akeshishi.tracker.MainActivity
import com.akeshishi.tracker.R
import com.akeshishi.tracker.base.extensions.getColorFromAttr
import com.akeshishi.tracker.base.extensions.makeGone
import com.akeshishi.tracker.base.extensions.makeVisible
import com.akeshishi.tracker.databinding.FragmentStatisticsBinding
import com.akeshishi.tracker.ui.viewmodels.StatisticsViewModel
import com.akeshishi.tracker.util.CustomMarkerView
import com.akeshishi.tracker.util.Utility
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private lateinit var viewBinding: FragmentStatisticsBinding
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        showEmptyPage()
        loadData()
        setupBarChart()
        backPressed()
    }

    private fun setupToolbar() {
        viewBinding.toolbar.txtTitle.text = getString(R.string.your_statistics)
    }

    private fun loadData() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner) { time ->
            time?.let {
                viewBinding.statisticsFragment.makeVisible()
                viewBinding.emptyPage.defaultEmptyPage.makeGone()
                val totalTimeRun = Utility.getStopWatchTime(time)
                viewBinding.txtTotalTime.text = totalTimeRun
            }
        }
        viewModel.totalDistance.observe(viewLifecycleOwner) { distance ->
            distance?.let {
                val km = distance / 1000f
                val totalDistance = (km * 10f).roundToInt() / 10f
                viewBinding.txtTotalDistance.text =
                    getString(R.string.total_distance, totalDistance.toString())
            }
        }
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner) { speed ->
            speed?.let {
                val avgSpeed = (speed * 10f).roundToInt() / 10f
                viewBinding.txtAverageSpeed.text =
                    getString(R.string.average_speed, avgSpeed.toString())
            }
        }
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) { calories ->
            calories?.let {
                viewBinding.txtTotalCalories.text =
                    getString(R.string.total_calories_burned, calories.toString())
            }
        }

        viewModel.actionsSortedByDate.observe(viewLifecycleOwner) { list ->
            list?.let {
                val allAvgSpeeds =
                    list.indices.map { index -> BarEntry(index.toFloat(), list[index].avgSpeed) }
                val barDataSet = BarDataSet(allAvgSpeeds, "Average speed over time").apply {
                    valueFormatter = CustomValueFormatter()
                    valueTextSize = 12F
                    valueTextColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
                    color = ContextCompat.getColor(requireContext(), R.color.amber_800)
                }

                viewBinding.barChart.data = BarData(barDataSet)
                viewBinding.barChart.marker =
                    CustomMarkerView(list.reversed(), requireContext(), R.layout.marker_view)
                viewBinding.barChart.invalidate()
            }
        }
    }

    private fun setupBarChart() {
        viewBinding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
            setDrawGridLines(false)
        }
        viewBinding.barChart.axisLeft.apply {
            textColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
            axisLineColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
            setDrawGridLines(false)
        }
        viewBinding.barChart.axisRight.apply {
            textColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
            axisLineColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
            setDrawGridLines(false)
        }
        viewBinding.barChart.apply {
            description.text = ""
            legend.textColor = requireContext().getColorFromAttr(R.attr.colorOnPrimary)
        }
    }

    private fun showEmptyPage() {
            viewBinding.statisticsFragment.makeGone()
            viewBinding.emptyPage.defaultEmptyPage.makeVisible()
            viewBinding.emptyPage.imgEmptyStatistics.makeVisible()
    }

    private fun backPressed() {
        requireActivity().onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        (requireActivity() as MainActivity).viewBinding
                            .bottomNavigation.selectedItemId = R.id.homeFragment
                    }
                }
            )
    }
}

/**
 * Class to format chart all values before they are drawn as labels.
 */
class CustomValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return DecimalFormat("#.#").format(value).toString()
    }
}