package com.arduinoworld.smartheater

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.preference.PreferenceManager
import com.arduinoworld.smartheater.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var vibrator: Vibrator
        lateinit var sharedPreferences: SharedPreferences
        lateinit var editPreferences: SharedPreferences.Editor
        lateinit var firebaseAuth: FirebaseAuth
        lateinit var realtimeDatabase: DatabaseReference

        var isHapticFeedbackEnabled = "1"
        var isUserLogged = false
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        editPreferences = sharedPreferences.edit()

        firebaseAuth = FirebaseAuth.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance().reference

        isHapticFeedbackEnabled = sharedPreferences.getString("isHapticFeedbackEnabled", "1").toString()
        isUserLogged = sharedPreferences.getBoolean("isUserLogged", false)

        with(binding) {
            if (isUserLogged) {
                setSupportActionBar(toolbarMainActivity)
                supportActionBar!!.title = getString(R.string.toolbar_main_activity)

                layoutWave.visibility = View.GONE
                layoutUserNotLogged.visibility = View.GONE
                layoutSignInUp.visibility = View.GONE
                appBarLayout.visibility = View.VISIBLE
                viewPager.visibility = View.VISIBLE

                val fragmentList = listOf(HumidityModeFragment(), TimeModeFragment())
                val adapter = ViewPagerAdapter(this@MainActivity, fragmentList)
                viewPager.adapter = adapter
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    if (position == 0) {
                        tab.text = getString(R.string.tab_item_temperature_mode)
                        tab.setIcon(R.drawable.ic_temperature)
                    } else {
                        tab.text = getString(R.string.tab_item_time_mode)
                        tab.setIcon(R.drawable.ic_time)
                    }
                }.attach()
            } else {
                layoutFirstWave.translationY = -575f
                layoutSecondWave.translationY = -575f

                imageMainUserProfile.alpha = 0f
                textUserNotLogged.alpha = 0f
                buttonSignIn.alpha = 0f
                buttonSignUp.alpha = 0f

                layoutSecondWave.animate().translationY(0f).setDuration(500).setStartDelay(0).start()
                layoutFirstWave.animate().translationY(0f).setDuration(500).setStartDelay(200).start()
                imageMainUserProfile.animate().alpha(1f).setDuration(500).setStartDelay(300).start()
                textUserNotLogged.animate().alpha(1f).setDuration(500).setStartDelay(300).start()
                buttonSignIn.animate().alpha(1f).setDuration(500).setStartDelay(300).start()
                buttonSignUp.animate().alpha(1f).setDuration(500).setStartDelay(300).start()

                buttonSignIn.setOnClickListener {
                    vibrate()
                    val activity = Intent(this@MainActivity, UserProfile::class.java)
                    startActivity(activity)
                }
                buttonSignUp.setOnClickListener {
                    vibrate()
                    val activity = Intent(this@MainActivity, SignUpActivity::class.java)
                    startActivity(activity)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val actionButtonsInflater = menuInflater
        actionButtonsInflater.inflate(R.menu.activity_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.buttonUserProfile -> {
                vibrate()
                val activity = Intent(this, UserProfile::class.java)
                startActivity(activity)
                true
            }
            R.id.buttonSettings -> {
                vibrate()
                val activity = Intent(this, SettingsActivity::class.java)
                startActivity(activity)
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        if (vibrator.hasVibrator()) {
            if (isHapticFeedbackEnabled == "1") {
                binding.buttonSignIn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(20)
                }
            }
        }
    }
}