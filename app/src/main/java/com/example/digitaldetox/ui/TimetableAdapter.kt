package com.example.digitaldetox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class TimetableAdapter(
    private val entries: MutableList<TimetableEntry>
) : RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder>() {

    inner class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTimeText: TextView = itemView.findViewById(R.id.textDayTime)
        val subjectText: TextView = itemView.findViewById(R.id.textSubject)
        val typeText: TextView = itemView.findViewById(R.id.textType)
//        val stepsTask: CheckBox = itemView.findViewById(R.id.checkSteps)
        val startButton: Button = itemView.findViewById(R.id.btnStartTimer)
        val timerText: TextView = itemView.findViewById(R.id.textTimer)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timetable_card, parent, false)
        return TimetableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimetableViewHolder, position: Int) {
        val entry = entries[position]

        holder.dayTimeText.text = "${entry.day} • ${entry.time}"
        holder.subjectText.text = entry.subject
        holder.typeText.text = "Type: ${entry.type.capitalize()}"
//        holder.stepsTask.isChecked = entry.stepsCompleted

//        holder.stepsTask.setOnCheckedChangeListener { _, isChecked ->
//            entry.stepsCompleted = isChecked
//            notifyItemChanged(position)
//        }


        holder.startButton.setOnClickListener {
            val timeRange = entry.time.split(" - ")
            if (timeRange.size == 2) {
                val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                try {
                    val start = format.parse(timeRange[0])
                    val end = format.parse(timeRange[1])
                    if (start != null && end != null) {
                        val durationInMillis = end.time - start.time
                        if (durationInMillis > 0) {
                            holder.startButton.isEnabled = false // 🔒 Disable Start button

                            object : android.os.CountDownTimer(durationInMillis, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    val mins = (millisUntilFinished / 1000) / 60
                                    val secs = (millisUntilFinished / 1000) % 60
                                    holder.timerText.text = "⏳ Remaining: $mins min $secs sec"
                                }

                                override fun onFinish() {
                                    holder.timerText.text = "✅ Session complete!"
                                    holder.startButton.isEnabled = true // ✅ Enable button again (optional)
                                }
                            }.start()
                        } else {
                            holder.timerText.text = "❗ Invalid time range"
                        }
                    }
                } catch (e: Exception) {
                    holder.timerText.text = "❗ Error parsing time"
                }
            } else {
                holder.timerText.text = "❗ Time format issue"
            }
        }




    }

    override fun getItemCount(): Int = entries.size
}
