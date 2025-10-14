package com.alvin.nutrigrow.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.alvin.nutrigrow.R
import com.alvin.nutrigrow.databinding.FragmentProfileBinding
import com.alvin.nutrigrow.ui.profile.adapter.SectionsPageAdapter
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        @StringRes
        private val TAB_TITLES = intArrayOf(
            R.string.profile_fragment1,
            R.string.profile_fragment2,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setTabLayout()
        setUserData()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setTabLayout() {
        val sectionsPageAdapter = SectionsPageAdapter(this)
        val profileViewPager: ViewPager2 = binding.profileViewPager
        profileViewPager.adapter = sectionsPageAdapter

        val profileTabs: TabLayout = binding.profileTabs
        TabLayoutMediator(profileTabs, profileViewPager) { tab, position ->
            tab.text = resources.getString(TAB_TITLES[position])
        }.attach()
    }

    private fun setUserData() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val user = auth.currentUser

        Glide.with(requireContext())
            .load(user?.photoUrl)
            .into(binding.imgUser)

        binding.tvDisplayName.text = user?.displayName
    }

}