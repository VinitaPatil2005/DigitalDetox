package com.example.digitaldetox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.digitaldetox.ui.SetLimitsActivity

class AppListAdapter(private val appList: List<SetLimitsActivity.AppItem>) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_checkbox, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = appList[position]
        holder.appName.text = app.name
        holder.checkBox.isChecked = app.isSelected

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            app.isSelected = isChecked
        }
    }

    override fun getItemCount(): Int = appList.size

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.appName)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }
}
