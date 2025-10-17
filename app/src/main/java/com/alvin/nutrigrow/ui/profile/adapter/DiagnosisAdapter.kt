package com.alvin.nutrigrow.ui.profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.data.Diagnosis

class DiagnosisAdapter(var listDiagnosis: List<Diagnosis>, val onClick: (Diagnosis) -> Unit) : RecyclerView.Adapter<DiagnosisAdapter.MainViewHolder>() {
    fun updateList(newList: List<Diagnosis>) {
        listDiagnosis = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diagnosis, parent, false)
        return MainViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MainViewHolder,
        position: Int
    ) {
        val diagnosis = listDiagnosis[position]

        holder.title.text = diagnosis.title
        holder.date.text = diagnosis.createdAt

        holder.itemView.setOnClickListener {
            onClick(diagnosis)
        }
    }

    override fun getItemCount(): Int {
        return listDiagnosis.size
    }

    class MainViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tvDiagnosisTitleItem)
        val date = view.findViewById<TextView>(R.id.tvDiagnosisDateItem)
    }
}