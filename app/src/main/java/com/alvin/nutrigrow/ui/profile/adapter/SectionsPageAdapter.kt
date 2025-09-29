package com.alvin.nutrigrow.ui.profile.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alvin.nutrigrow.ui.profile.tabs.MyDiagnosisFragment
import com.alvin.nutrigrow.ui.profile.tabs.MyPostFragment

class SectionsPageAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    private val listFragment = listOf<Fragment>(
        MyPostFragment(), MyDiagnosisFragment()
    )

    override fun createFragment(position: Int): Fragment {
        val fragment = listFragment[position]
        return fragment
    }

    override fun getItemCount(): Int {
        return 2
    }
}