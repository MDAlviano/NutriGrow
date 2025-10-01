package com.alvin.nutrigrow.ui.plantplan.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Progress

class PlantPlanProgressAdapter(val listProgress: List<Progress>, val onClick: (Progress) -> Unit) : RecyclerView.Adapter<PlantPlanProgressAdapter.MainViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant_condition, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MainViewHolder,
        position: Int
    ) {
        val progress = listProgress[position]
        holder.day.text = "Hari ke - ${progress.day}"

        holder.itemView.setOnClickListener {
            onClick(progress)
        }
    }

    override fun getItemCount(): Int {
        return listProgress.size
    }

    class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val day = view.findViewById<TextView>(R.id.tvPlantConditionItem)
    }
}