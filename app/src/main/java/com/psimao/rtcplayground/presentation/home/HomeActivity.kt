package com.psimao.rtcplayground.presentation.home

import android.Manifest
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.psimao.rtcplayground.R
import com.psimao.rtcplayground.presentation.home.dial.DialFragment
import com.psimao.rtcplayground.presentation.home.settings.SettingsFragment
import com.psimao.rtcplayground.presentation.socket.SocketService
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1
    }

    private val dialFragment: DialFragment by lazy { DialFragment() }
    private val settingsFragment: SettingsFragment by lazy { SettingsFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        navigation.setOnNavigationItemSelectedListener(BottomNavigationListener())
        ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.ACCESS_NETWORK_STATE),
                PERMISSIONS_REQUEST_CODE
        )
        if (SocketService.isConnected) {
            showDialScreen()
        } else {
            navigation.selectedItemId = R.id.navigation_settings
            showSettingsScreen()
        }
    }

    private fun showDialScreen() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, dialFragment, DialFragment.TAG)
                .commit()
    }

    private fun showSettingsScreen() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment, SettingsFragment.TAG)
                .commit()
    }

    inner class BottomNavigationListener : BottomNavigationView.OnNavigationItemSelectedListener {

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_home -> showDialScreen()
                R.id.navigation_settings -> showSettingsScreen()
                else -> return false
            }
            return true
        }
    }
}
