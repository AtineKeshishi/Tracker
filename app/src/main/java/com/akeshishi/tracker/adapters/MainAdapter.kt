package com.akeshishi.tracker.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akeshishi.tracker.R
import com.akeshishi.tracker.base.db.Action
import com.akeshishi.tracker.databinding.ItemActionListBinding
import com.akeshishi.tracker.util.Utility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class MainAdapter(
    private val context: Context,
    private val list: List<Action>,
    private val getAction: (Int) -> Unit
) : RecyclerView.Adapter<MainAdapter.RunViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val viewBinding =
            ItemActionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunViewHolder(viewBinding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) =
        holder.bind(list[position])

    inner class RunViewHolder(private val viewBinding: ItemActionListBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(action: Action) {
            Glide.with(context).load(action.img).into(viewBinding.imgRunImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = action.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            viewBinding.txtDate.text =
                context.resources.getString(R.string.date, dateFormat.format(calendar.time))
            viewBinding.txtTime.text = context.resources.getString(
                R.string.time,
                Utility.getStopWatchTime(action.timeInMillis)
            )
            viewBinding.txtAvgSpeed.text =
                context.resources.getString(R.string.avg_speed, action.avgSpeed.toString())
            viewBinding.txtDistance.text =
                context.resources.getString(
                    R.string.distance,
                    (action.distance / 1000f).toString()
                )
            viewBinding.txtCalories.text =
                context.resources.getString(R.string.calories, action.burnedCalories.toString())

            itemView.setOnLongClickListener {
                getAction(action.id!!)
                true
            }
        }
    }
}