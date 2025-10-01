package com.alvin.nutrigrow.ui.plantplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Plan

class PlantPlanAdapter(val listPlantPlan: List<Plan>, val onClick: (Plan) -> Unit): RecyclerView.Adapter<PlantPlanAdapter.MainViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plantplan, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MainViewHolder,
        position: Int
    ) {
        val plantPlan = listPlantPlan[position]

        holder.title.text = plantPlan.name
        holder.growingMediaAndDay.text = "${plantPlan.growingMedia} - Hari ke ${plantPlan.day}"

        holder.itemView.setOnClickListener {
            onClick(plantPlan)
        }
    }

    override fun getItemCount(): Int {
        return listPlantPlan.size
    }

    class MainViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tvPlantPlantTitleItem)
        val growingMediaAndDay = view.findViewById<TextView>(R.id.tvPlantPlanGrowingMediaAndDayItem)
    }
}