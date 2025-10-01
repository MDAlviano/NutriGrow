package com.alvin.nutrigrow.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Plan

class ContinuePlantPlanAdapter(val listPlantPlan: List<Plan>, val onClick: (Plan) -> Unit): RecyclerView.Adapter<ContinuePlantPlanAdapter.MainViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_continue_plant, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MainViewHolder,
        position: Int
    ) {
        val plantPlan = listPlantPlan[position]

        holder.title.text = plantPlan.name

        holder.link.setOnClickListener {
            onClick(plantPlan)
        }
    }

    override fun getItemCount(): Int {
        return listPlantPlan.size
    }

    class MainViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tvContinuePlantPlantTitleItem)
        val link = view.findViewById<TextView>(R.id.tvLinkContinue)
    }
}