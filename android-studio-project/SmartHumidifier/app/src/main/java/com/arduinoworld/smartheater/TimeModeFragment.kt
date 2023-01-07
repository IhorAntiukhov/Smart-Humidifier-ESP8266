package com.arduinoworld.smartheater

import android.animation.Animator
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.text.format.DateFormat.is24HourFormat
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.arduinoworld.smartheater.MainActivity.Companion.editPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.firebaseAuth
import com.arduinoworld.smartheater.MainActivity.Companion.isHapticFeedbackEnabled
import com.arduinoworld.smartheater.MainActivity.Companion.realtimeDatabase
import com.arduinoworld.smartheater.MainActivity.Companion.sharedPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.vibrator
import com.arduinoworld.smartheater.databinding.FragmentTimeModeBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class TimeModeFragment : Fragment() {
    private lateinit var binding: FragmentTimeModeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            var isHeaterOnTime = false
            var timestampArrayList = ArrayList<HeaterTimestamp>()

            val isSystem24Hour = is24HourFormat(requireActivity())
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val calendar = Calendar.getInstance()

            val gson = Gson()
            if (sharedPreferences.getBoolean("TimeModeStarted", false)) {
                layoutTime.visibility = View.GONE
                buttonStartTimeMode.visibility = View.GONE
                buttonStopTimeMode.visibility = View.VISIBLE
            }
            if (sharedPreferences.getString("TimestampArrayList", "") != "")
                timestampArrayList = gson.fromJson(sharedPreferences.getString("TimestampArrayList", ""), object : TypeToken<ArrayList<HeaterTimestamp?>?>() {}.type)

            val recyclerAdapter = RecyclerAdapter(timestampArrayList)
            recyclerView.apply {
                adapter = recyclerAdapter
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }

            buttonDeleteTime.setOnClickListener {
                vibrate()
                if (timestampArrayList.size >= 1) {
                    val timestampArrayListSize = timestampArrayList.size
                    timestampArrayList.clear()
                    recyclerAdapter.notifyItemRangeRemoved(0, timestampArrayListSize)
                    isHeaterOnTime = false
                    editPreferences.putString("TimestampArrayList", "").apply()
                } else {
                    Toast.makeText(requireActivity(), "Добавьте хотя бы\nодно время включения\n/выключения!", Toast.LENGTH_LONG).show()
                }
            }

            buttonAddTime.setOnClickListener {
                vibrate()
                val timePicker = MaterialTimePicker.Builder()
                        .setTimeFormat(clockFormat)
                        .setTitleText("Выберите время")
                        .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                        .setMinute(calendar.get(Calendar.MINUTE))
                        .build()

                timePicker.addOnPositiveButtonClickListener {
                    vibrate()
                    isHeaterOnTime = !isHeaterOnTime
                    var hour = timePicker.hour.toString()
                    var minute = timePicker.minute.toString()
                    if (hour.toInt() < 10) {
                        hour = "0$hour"
                    }
                    if (minute.toInt() < 10) {
                        minute = "0$minute"
                    }
                    timestampArrayList.add(HeaterTimestamp("$hour:$minute", isHeaterOnTime))
                    recyclerAdapter.notifyItemInserted(timestampArrayList.size - 1)
                }
                timePicker.addOnNegativeButtonClickListener {
                    vibrate()
                }
                timePicker.show(requireActivity().supportFragmentManager, "timePicker")
            }

            var isAnimationStarted: Boolean
            buttonStartTimeMode.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    if (sharedPreferences.getInt("Temperature", -1) == -1) {
                        if (!sharedPreferences.getBoolean("isHeaterStarted", false)) {
                            if (timestampArrayList.size >= 1) {
                                isAnimationStarted = false
                                var heaterOnOffTimeNodeValue = ""
                                timestampArrayList.forEach {
                                    heaterOnOffTimeNodeValue += it.time.removeRange(2, 3)
                                }
                                editPreferences.putBoolean("TimeModeStarted", true)
                                editPreferences.putString("TimestampArrayList", gson.toJson(timestampArrayList)).apply()

                                realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidifierOnOffTime").setValue(heaterOnOffTimeNodeValue)
                                if (sharedPreferences.getBoolean("isHeaterStarted", false)) {
                                    realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted")
                                        .setValue(false)
                                    editPreferences.putBoolean("isHeaterStarted", false).apply()
                                }

                                layoutTime.translationX = 0f
                                buttonStartTimeMode.translationX = 0f

                                layoutTime.alpha = 1f
                                buttonStartTimeMode.alpha = 1f

                                layoutTime.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0).start()
                                buttonStartTimeMode.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(100)
                                    .setListener(object: Animator.AnimatorListener {
                                        override fun onAnimationStart(p0: Animator) {}

                                        override fun onAnimationEnd(p0: Animator) {
                                            if (!isAnimationStarted) {
                                                isAnimationStarted = true
                                                layoutTime.visibility = View.GONE
                                                buttonStartTimeMode.visibility = View.GONE
                                                buttonStopTimeMode.visibility = View.VISIBLE

                                                buttonStopTimeMode.translationX = 800f
                                                buttonStopTimeMode.alpha = 0f
                                                buttonStopTimeMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                                            }
                                        }

                                        override fun onAnimationCancel(p0: Animator) {}
                                        override fun onAnimationRepeat(p0: Animator) {}

                                    }).start()
                            } else {
                                Toast.makeText(requireActivity(), "Добавьте хотя бы\nодно время включения\n/выключения!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(requireActivity(), "Сейчас запущен обогреватель!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Сейчас запущен режим по температуре!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonStopTimeMode.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    isAnimationStarted = false
                    editPreferences.putBoolean("TimeModeStarted", false).apply()

                    realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidifierOnOffTime").setValue(" ")

                    buttonStopTimeMode.translationX = 0f
                    buttonStopTimeMode.alpha = 1f
                    buttonStopTimeMode.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0)
                        .setListener(object: Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator) {}

                            override fun onAnimationEnd(p0: Animator) {
                                if (!isAnimationStarted) {
                                    isAnimationStarted = true
                                    buttonStopTimeMode.visibility = View.GONE
                                    layoutTime.visibility = View.VISIBLE
                                    buttonStartTimeMode.visibility = View.VISIBLE

                                    layoutTime.translationX = 800f
                                    buttonStartTimeMode.translationX = 800f

                                    layoutTime.alpha = 0f
                                    buttonStartTimeMode.alpha = 0f

                                    layoutTime.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                                    buttonStartTimeMode.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                                }
                            }

                            override fun onAnimationCancel(p0: Animator) {}
                            override fun onAnimationRepeat(p0: Animator) {}

                        }).start()
                } else {
                    Toast.makeText(requireActivity(), "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimeModeBinding.inflate(layoutInflater, container, false)
        return binding.root
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
                binding.buttonStartTimeMode.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
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