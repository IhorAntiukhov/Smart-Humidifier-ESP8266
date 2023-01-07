package com.arduinoworld.smartheater

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arduinoworld.smartheater.MainActivity.Companion.editPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.isHapticFeedbackEnabled
import com.arduinoworld.smartheater.MainActivity.Companion.vibrator
import com.arduinoworld.smartheater.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var binding: ActivitySettingsBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSettings)
        supportActionBar!!.title = getString(R.string.toolbar_settings)

        setTheme(R.style.SettingsStyle)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayoutSettings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val reducingTheMinTemperature: Preference = preferenceManager.findPreference("ReducingTheMinTemperature")!!
            val increasingTheMaxTemperature: Preference = preferenceManager.findPreference("IncreasingTheMaxTemperature")!!
            val isHapticFeedbackEnabled: Preference = preferenceManager.findPreference("isHapticFeedbackEnabled")!!
            val buttonDefaultSettings: Preference = preferenceManager.findPreference("buttonDefaultSettings")!!

            reducingTheMinTemperature.setOnPreferenceClickListener {
                vibrate()
                return@setOnPreferenceClickListener true
            }
            reducingTheMinTemperature.setOnPreferenceChangeListener { _, _ ->
                vibrate()
                return@setOnPreferenceChangeListener true
            }

            increasingTheMaxTemperature.setOnPreferenceClickListener {
                vibrate()
                return@setOnPreferenceClickListener true
            }
            increasingTheMaxTemperature.setOnPreferenceChangeListener { _, _ ->
                vibrate()
                return@setOnPreferenceChangeListener true
            }

            isHapticFeedbackEnabled.setOnPreferenceClickListener {
                vibrate()
                return@setOnPreferenceClickListener true
            }
            isHapticFeedbackEnabled.setOnPreferenceChangeListener { _, newValue ->
                vibrate()
                MainActivity.isHapticFeedbackEnabled = newValue as String
                return@setOnPreferenceChangeListener true
            }

            buttonDefaultSettings.setOnPreferenceClickListener {
                vibrate()
                MainActivity.isHapticFeedbackEnabled = "1"
                editPreferences.putBoolean("ShowNotificationWithTemperature", false)
                editPreferences.putBoolean("EnableHeaterStateNotifications", true)
                editPreferences.putString("ReducingTheMinTemperature", "2")
                editPreferences.putString("IncreasingTheMaxTemperature", "1")
                editPreferences.putString("isHapticFeedbackEnabled", "1").apply()
                Toast.makeText(requireActivity(), "Настройки по умолчанию\nсохранены в память!", Toast.LENGTH_SHORT).show()
                return@setOnPreferenceClickListener true
            }
        }

        @Suppress("DEPRECATION")
        private fun vibrate() {
            if (vibrator.hasVibrator()) {
                if (isHapticFeedbackEnabled == "1") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        binding.buttonForHapticFeedback.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
                    } else {
                        binding.buttonForHapticFeedback.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING + HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                    }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val actionButtonsInflater = menuInflater
        actionButtonsInflater.inflate(R.menu.settings_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.buttonHome -> {
                vibrate()
                val activity = Intent(this, MainActivity::class.java)
                startActivity(activity)
                true
            }
            R.id.buttonUserProfile -> {
                vibrate()
                val activity = Intent(this, UserProfile::class.java)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    binding.buttonForHapticFeedback.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
                } else {
                    binding.buttonForHapticFeedback.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING + HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                }
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