package com.arduinoworld.smartheater

import android.animation.Animator
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.arduinoworld.smartheater.MainActivity.Companion.editPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.firebaseAuth
import com.arduinoworld.smartheater.MainActivity.Companion.isHapticFeedbackEnabled
import com.arduinoworld.smartheater.MainActivity.Companion.isUserLogged
import com.arduinoworld.smartheater.MainActivity.Companion.realtimeDatabase
import com.arduinoworld.smartheater.MainActivity.Companion.sharedPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.vibrator
import com.arduinoworld.smartheater.databinding.FragmentHumidityModeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HumidityModeFragment : Fragment() {
    private lateinit var binding: FragmentHumidityModeBinding
    private var isHeaterStarted = false
    private var isAnimationStarted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            textTemperature.text = getString(R.string.text_temperature, sharedPreferences.getInt("LastTemperature", 0))
            progressBarTemperature.progress = sharedPreferences.getInt("LastTemperature", 0)
            textHumidity.text = getString(R.string.text_temperature, sharedPreferences.getInt("LastHumidity", 0))
            progressBarHumidity.progress = sharedPreferences.getInt("LastHumidity", 0)

            if (sharedPreferences.getInt("Temperature", -1) == -1) {
                layoutTemperatureHumidity.translationX = 800f
                inputLayoutTemperature.translationX = 800f
                buttonStartTemperatureMode.translationX = 800f

                layoutTemperatureHumidity.alpha = 0f
                inputLayoutTemperature.alpha = 0f
                buttonStartTemperatureMode.alpha = 0f

                layoutTemperatureHumidity.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                inputLayoutTemperature.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                buttonStartTemperatureMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                buttonStartTemperatureMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
                if (sharedPreferences.getBoolean("isHeaterStarted", false)) {
                    isHeaterStarted = true
                    buttonStartHeater.visibility = View.GONE
                    buttonStopHeater.visibility = View.VISIBLE
                    buttonStopHeater.translationX = 800f
                    buttonStopHeater.alpha = 0f
                    buttonStopHeater.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(400).start()
                } else {
                    buttonStartHeater.translationX = 800f
                    buttonStartHeater.alpha = 0f
                    buttonStartHeater.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(400).start()
                }
            } else {
                inputLayoutTemperature.visibility = View.GONE
                buttonStartTemperatureMode.visibility = View.GONE
                buttonStartHeater.visibility = View.GONE
                cardViewTemperatureRange.visibility = View.VISIBLE
                cardViewHeaterStarted.visibility = View.VISIBLE
                buttonStopTemperatureMode.visibility = View.VISIBLE

                realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted")
                    .addValueEventListener(isHeaterStartedListener)

                inputTemperature.setText(sharedPreferences.getInt("Temperature", -1).toString())
                val minTemperature = inputTemperature.text.toString().toInt() - sharedPreferences.getString("ReducingTheMinTemperature", "5").toString().toInt()
                val maxTemperature = inputTemperature.text.toString().toInt() + sharedPreferences.getString("IncreasingTheMaxTemperature", "5").toString().toInt()
                textTemperatureRange.text = getString(R.string.card_view_temperature_range, minTemperature, maxTemperature)

                layoutTemperatureHumidity.translationX = 800f
                cardViewTemperatureRange.translationX = 800f
                cardViewHeaterStarted.translationX = 800f
                buttonStopTemperatureMode.translationX = 800f

                layoutTemperatureHumidity.alpha = 0f
                cardViewTemperatureRange.alpha = 0f
                cardViewHeaterStarted.alpha = 0f
                buttonStopTemperatureMode.alpha = 0f

                layoutTemperatureHumidity.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                cardViewTemperatureRange.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                cardViewHeaterStarted.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
                buttonStopTemperatureMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(400).start()
            }

            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("temperature")
                .addValueEventListener(temperatureListener)
            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidity")
                .addValueEventListener(humidityListener)

            buttonStartTemperatureMode.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    if (!sharedPreferences.getBoolean("TimeModeStarted", false)) {
                        if (!sharedPreferences.getBoolean("isHeaterStarted", false)) {
                            if (inputTemperature.text!!.isNotEmpty()) {
                                hideKeyboard()
                                isAnimationStarted = false
                                editPreferences.putInt("Temperature", inputTemperature.text!!.toString().toInt()).apply()

                                val minTemperature = inputTemperature.text.toString().toInt() - sharedPreferences.getString("ReducingTheMinTemperature", "5").toString().toInt()
                                val maxTemperature = inputTemperature.text.toString().toInt() + sharedPreferences.getString("IncreasingTheMaxTemperature", "5").toString().toInt()
                                realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidityRange")
                                    .setValue("$minTemperature $maxTemperature")
                                realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted")
                                    .addValueEventListener(isHeaterStartedListener)
                                textTemperatureRange.text = getString(R.string.card_view_temperature_range, minTemperature, maxTemperature)

                                layoutTemperatureHumidity.translationX = 0f
                                inputLayoutTemperature.translationX = 0f
                                buttonStartTemperatureMode.translationX = 0f

                                inputLayoutTemperature.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0).start()
                                buttonStartTemperatureMode.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(100).start()
                                if (!isHeaterStarted) {
                                    buttonStartHeater.translationX = 0f
                                    buttonStartHeater.alpha = 1f
                                    buttonStartHeater.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(200).setListener(animatorListener)
                                } else {
                                    buttonStopHeater.translationX = 0f
                                    buttonStopHeater.alpha = 1f
                                    buttonStartHeater.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(200).setListener(animatorListener)
                                }
                            } else {
                                inputLayoutTemperature.isErrorEnabled = true
                                inputLayoutTemperature.error = "Введите температуру обогрева"
                            }
                        } else {
                            Toast.makeText(activity, "Сейчас запущен обогреватель!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(activity, "Сейчас запущен\nрежим по времени!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(activity, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonStopTemperatureMode.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    isAnimationStarted = false
                    editPreferences.putInt("Temperature", -1).apply()

                    realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidityRange").setValue(" ")
                    realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted")
                        .removeEventListener(isHeaterStartedListener)

                    cardViewTemperatureRange.translationX = 0f
                    cardViewHeaterStarted.translationX = 0f
                    buttonStopTemperatureMode.translationX = 0f

                    cardViewTemperatureRange.alpha = 1f
                    cardViewHeaterStarted.alpha = 1f
                    buttonStopTemperatureMode.alpha = 1f

                    cardViewTemperatureRange.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0).start()
                    cardViewHeaterStarted.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(100).start()
                    buttonStopTemperatureMode.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(200)
                        .setListener(object: Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator) {}

                            override fun onAnimationEnd(p0: Animator) {
                                if (!isAnimationStarted) {
                                    isAnimationStarted = true
                                    cardViewTemperatureRange.visibility = View.GONE
                                    cardViewHeaterStarted.visibility = View.GONE
                                    buttonStopTemperatureMode.visibility = View.GONE

                                    inputLayoutTemperature.visibility = View.VISIBLE
                                    buttonStartTemperatureMode.visibility = View.VISIBLE
                                    buttonStartHeater.visibility = View.VISIBLE

                                    inputLayoutTemperature.translationX = 1000f
                                    buttonStartTemperatureMode.translationX = 1000f
                                    buttonStartHeater.translationX = 1000f

                                    inputLayoutTemperature.alpha = 1f
                                    buttonStartTemperatureMode.alpha = 1f
                                    buttonStartHeater.alpha = 1f

                                    inputLayoutTemperature.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                                    buttonStartTemperatureMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                                    buttonStartHeater.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                                }
                            }

                            override fun onAnimationCancel(p0: Animator) {}
                            override fun onAnimationRepeat(p0: Animator) {}

                        }).start()
                } else {
                    Toast.makeText(activity, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonStartHeater.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    if (!sharedPreferences.getBoolean("TimeModeStarted", false)) {
                        isAnimationStarted = false
                        isHeaterStarted = true
                        editPreferences.putBoolean("isHeaterStarted", true).apply()

                        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted").setValue(true)

                        buttonStartHeater.translationX = 0f
                        buttonStartHeater.alpha = 1f
                        buttonStartHeater.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0)
                            .setListener(object: Animator.AnimatorListener {
                                override fun onAnimationStart(p0: Animator) {}

                                override fun onAnimationEnd(p0: Animator) {
                                    if (!isAnimationStarted) {
                                        isAnimationStarted = true
                                        buttonStartHeater.visibility = View.GONE
                                        buttonStopHeater.visibility = View.VISIBLE

                                        buttonStopHeater.translationX = 800f
                                        buttonStopHeater.alpha = 0f
                                        buttonStopHeater.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                                    }
                                }

                                override fun onAnimationCancel(p0: Animator) {}
                                override fun onAnimationRepeat(p0: Animator) {}

                            }).start()
                    } else {
                        Toast.makeText(activity, "Сейчас запущен\nрежим по времени!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(activity, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonStopHeater.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    isAnimationStarted = false
                    isHeaterStarted = false
                    realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted").setValue(false)
                    editPreferences.putBoolean("isHeaterStarted", false).apply()

                    buttonStopHeater.translationX = 0f
                    buttonStopHeater.alpha = 1f
                    buttonStopHeater.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0)
                        .setListener(object: Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator) {}

                            override fun onAnimationEnd(p0: Animator) {
                                if (!isAnimationStarted) {
                                    isAnimationStarted = true
                                    buttonStartHeater.visibility = View.VISIBLE
                                    buttonStopHeater.visibility = View.GONE

                                    buttonStartHeater.translationX = 800f
                                    buttonStartHeater.alpha = 0f
                                    buttonStartHeater.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                                }
                            }

                            override fun onAnimationCancel(p0: Animator) {}
                            override fun onAnimationRepeat(p0: Animator) {}

                        }).start()
                } else {
                    Toast.makeText(activity, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHumidityModeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private val animatorListener = object: Animator.AnimatorListener {
        override fun onAnimationStart(p0: Animator) {}

        override fun onAnimationEnd(p0: Animator) {
            if (!isAnimationStarted) {
                with(binding) {
                    isAnimationStarted = true
                    inputLayoutTemperature.visibility = View.GONE
                    buttonStartTemperatureMode.visibility = View.GONE
                    buttonStartHeater.visibility = View.GONE
                    buttonStopHeater.visibility = View.GONE

                    cardViewTemperatureRange.visibility = View.VISIBLE
                    cardViewHeaterStarted.visibility = View.VISIBLE
                    buttonStopTemperatureMode.visibility = View.VISIBLE

                    cardViewTemperatureRange.translationX = 800f
                    cardViewHeaterStarted.translationX = 800f
                    buttonStopTemperatureMode.translationX = 800f

                    cardViewTemperatureRange.alpha = 0f
                    cardViewHeaterStarted.alpha = 0f
                    buttonStopTemperatureMode.alpha = 0f

                    cardViewTemperatureRange.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                    cardViewHeaterStarted.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                    buttonStopTemperatureMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                }
            }
        }

        override fun onAnimationCancel(p0: Animator) {}
        override fun onAnimationRepeat(p0: Animator) {}

    }

    override fun onDestroy() {
        super.onDestroy()
        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("temperature")
            .removeEventListener(temperatureListener)
        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidity")
            .removeEventListener(temperatureListener)
        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted")
            .removeEventListener(isHeaterStartedListener)
    }

    private val isHeaterStartedListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.getValue(Boolean::class.java)!!) {
                binding.textHeaterStarted.text = getString(R.string.heater_started)
            } else {
                binding.textHeaterStarted.text = getString(R.string.heater_stopped)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            if (isUserLogged) Toast.makeText(activity, "Не удалось получить\nто, запущен ли\nобогреватель!", Toast.LENGTH_LONG).show()
        }

    }

    private val temperatureListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val snapshotValue = snapshot.getValue(Int::class.java)!!
            binding.textTemperature.text = requireActivity().getString(R.string.text_temperature, snapshotValue)
            binding.progressBarTemperature.progress = snapshotValue
            editPreferences.putInt("LastTemperature", snapshotValue).apply()
        }

        override fun onCancelled(error: DatabaseError) {
            if (isUserLogged) Toast.makeText(activity, "Не удалось получить температуру!", Toast.LENGTH_LONG).show()
        }

    }

    private val humidityListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val snapshotValue = snapshot.getValue(Int::class.java)!!
            binding.textHumidity.text = getString(R.string.text_humidity, snapshotValue)
            binding.progressBarHumidity.progress = snapshotValue
            editPreferences.putInt("LastHumidity", snapshotValue).apply()
        }

        override fun onCancelled(error: DatabaseError) {
            if (isUserLogged) Toast.makeText(activity, "Не удалось получить температуру!", Toast.LENGTH_LONG).show()
        }

    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @Suppress("DEPRECATION")
    private fun isNetworkConnected() : Boolean {
        val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        if (vibrator.hasVibrator()) {
            if (isHapticFeedbackEnabled == "1") {
                binding.buttonStartTemperatureMode.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
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