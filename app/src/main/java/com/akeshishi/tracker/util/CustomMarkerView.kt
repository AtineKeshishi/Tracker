package com.akeshishi.tracker.util

import android.content.Context
import android.view.LayoutInflater
import com.akeshishi.tracker.R
import com.akeshishi.tracker.base.db.Action
import com.akeshishi.tracker.databinding.MarkerViewBinding
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(private val list: List<Action>, context: Context, layoutId: Int)
    : MarkerView(context, layoutId) {

    private var viewBinding = MarkerViewBinding.inflate(LayoutInflater.from(context)).also {
        addView(it.root)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        super.refreshContent(entry, highlight)

        if (entry == null) return

        val run = list[entry.x.toInt()]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        viewBinding.txtDate.text =
            context.resources.getString(R.string.date, dateFormat.format(calendar.time))
        viewBinding.txtDuration.text = context.resources.getString(
            R.string.time,
            Utility.getStopWatchTime(run.timeInMillis)
        )
        viewBinding.txtAvgSpeed.text =
            context.resources.getString(R.string.avg_speed, run.avgSpeed.toString())
        viewBinding.txtDistance.text =
            context.resources.getString(R.string.distance, (run.distance / 1000f).toString())
        viewBinding.txtCaloriesBurned.text =
            context.resources.getString(R.string.calories, run.burnedCalories.toString())
    }
}