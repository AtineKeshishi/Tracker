package com.akeshishi.tracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.akeshishi.tracker.base.extensions.makeGone
import com.akeshishi.tracker.base.extensions.makeVisible
import com.akeshishi.tracker.databinding.ActivityMainBinding
import com.akeshishi.tracker.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setupBottomNavigation()
        navigateToTrackingFragment(intent)
    }

    // If the Activity wasn't destroyed, we use the pending intent to launch it,
    // in this case, onCreate() won't be called, instead, onNewIntent() will be called.
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    private fun setupBottomNavigation() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        viewBinding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.accountFragment, R.id.homeFragment, R.id.statisticsFragment ->
                    viewBinding.bottomNavigation.makeVisible()
                else -> viewBinding.bottomNavigation.makeGone()
            }
        }
    }

    private fun navigateToTrackingFragment(intent: Intent?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT)
            navController.navigate(R.id.globalActionToTrackingFragment)
    }
}




