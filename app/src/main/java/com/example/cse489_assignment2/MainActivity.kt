package com.example.cse489_assignment2

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.cse489_assignment2.broadcast.BroadcastDataFragment
import com.example.cse489_assignment2.broadcast.BroadcastResultFragment
import com.example.cse489_assignment2.broadcast.BroadcastSelectionFragment
import com.example.cse489_assignment2.databinding.ActivityMainBinding
import com.example.cse489_assignment2.features.AudioFragment
import com.example.cse489_assignment2.features.ImageScaleFragment
import com.example.cse489_assignment2.features.VideoFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBar.toolbar)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBar.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        drawerToggle.setToolbarNavigationClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val onChildScreen = supportFragmentManager.backStackEntryCount > 0
            drawerToggle.isDrawerIndicatorEnabled = !onChildScreen
            supportActionBar?.setDisplayHomeAsUpEnabled(onChildScreen)
            drawerToggle.syncState()
            updateTitleForVisibleFragment()
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            val destination = when (item.itemId) {
                R.id.broadcastSelectionFragment -> BroadcastSelectionFragment()
                R.id.imageScaleFragment -> ImageScaleFragment()
                R.id.videoFragment -> VideoFragment()
                R.id.audioFragment -> AudioFragment()
                else -> return@setNavigationItemSelectedListener false
            }
            supportFragmentManager.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            showFragment(destination, addToBackStack = false)
            item.isChecked = true
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        if (savedInstanceState == null) {
            binding.navigationView.setCheckedItem(R.id.broadcastSelectionFragment)
            showFragment(BroadcastSelectionFragment(), addToBackStack = false)
        }
    }

    fun navigateTo(fragment: Fragment) {
        showFragment(fragment, addToBackStack = true)
    }

    fun restartBroadcastFlow() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) transaction.addToBackStack(fragment::class.java.simpleName)
        transaction.commit()
        title = titleFor(fragment)
    }

    private fun updateTitleForVisibleFragment() {
        val visible = supportFragmentManager.findFragmentById(R.id.fragment_container) ?: return
        title = titleFor(visible)
    }

    private fun titleFor(fragment: Fragment): String = getString(
        when (fragment) {
            is BroadcastSelectionFragment -> R.string.menu_broadcast
            is BroadcastDataFragment -> R.string.broadcast_step_two
            is BroadcastResultFragment -> R.string.broadcast_step_three
            is ImageScaleFragment -> R.string.menu_image_scale
            is VideoFragment -> R.string.menu_video
            is AudioFragment -> R.string.menu_audio
            else -> R.string.app_name
        }
    )

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) ->
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount > 0 ->
                supportFragmentManager.popBackStack()
            else -> super.onBackPressed()
        }
    }
}
