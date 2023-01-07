package com.arduinoworld.smartheater

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.util.Patterns
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.arduinoworld.smartheater.MainActivity.Companion.editPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.firebaseAuth
import com.arduinoworld.smartheater.MainActivity.Companion.isHapticFeedbackEnabled
import com.arduinoworld.smartheater.MainActivity.Companion.isUserLogged
import com.arduinoworld.smartheater.MainActivity.Companion.sharedPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.vibrator
import com.arduinoworld.smartheater.databinding.ActivitySignUpBinding
import com.arduinoworld.smartheater.databinding.LoadingDialogBinding
import com.google.firebase.auth.EmailAuthProvider

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            if (intent.hasExtra("LaunchReason")) {
                textRegister.visibility = View.GONE
                buttonCreateUser.visibility = View.GONE
                buttonUpdateUser.visibility = View.VISIBLE
                textUpdateUser.visibility = View.VISIBLE

                if (intent.getIntExtra("LaunchReason", 0) == 0) {
                    inputLayoutUserPassword.visibility = View.GONE
                    inputLayoutConfirmPassword.visibility = View.GONE
                    textUpdateUser.text = getString(R.string.text_update_email)

                    layoutFirstWave.translationY = -575f
                    layoutSecondWave.translationY = -575f
                    inputLayoutUserEmail.translationX = 800f
                    buttonUpdateUser.translationX = 800f

                    inputLayoutUserEmail.alpha = 0f
                    buttonUpdateUser.alpha = 0f

                    layoutSecondWave.animate().translationY(0f).setDuration(500).setStartDelay(0).start()
                    layoutFirstWave.animate().translationY(0f).setDuration(500).setStartDelay(200).start()
                    inputLayoutUserEmail.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                    buttonUpdateUser.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()

                    buttonUpdateUser.setOnClickListener {
                        vibrate()
                        if (isNetworkConnected()) {
                            if (inputUserEmail.text!!.isNotEmpty() && isValidEmail(inputUserEmail.text!!.toString())
                                && inputUserEmail.text!!.toString() != sharedPreferences.getString("UserEmail", "")) {
                                hideKeyboard()
                                firebaseAuth.currentUser!!.reauthenticate(
                                    EmailAuthProvider.getCredential(sharedPreferences.getString("UserEmail", "").toString(),
                                        sharedPreferences.getString("UserPassword", "").toString())).addOnCompleteListener { reauthTask ->
                                    if (reauthTask.isSuccessful) {
                                        firebaseAuth.currentUser!!.updateEmail(inputUserEmail.text!!.toString())
                                        editPreferences.putString("UserEmail", inputUserEmail.text.toString()).apply()
                                        Toast.makeText(baseContext, "Почта пользователя обновлена!", Toast.LENGTH_SHORT).show()
                                        val activity = Intent(this@SignUpActivity, UserProfile::class.java)
                                        startActivity(activity)
                                        finish()
                                    } else {
                                        editPreferences.putBoolean("isUserLogged", false).apply()
                                        isUserLogged = false
                                        Toast.makeText(baseContext, "Не удалось переавторизироваться!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                if (inputUserEmail.text!!.isEmpty()) {
                                    inputLayoutUserEmail.isErrorEnabled = true
                                    inputLayoutUserEmail.error = "Введите почту пользователя"
                                } else {
                                    if (!isValidEmail(inputUserEmail.text.toString())) {
                                        inputLayoutUserEmail.isErrorEnabled = true
                                        inputLayoutUserEmail.error = "Неправильная почта"
                                    } else {
                                        if (inputUserEmail.text!!.toString() == sharedPreferences.getString("UserEmail", "")) {
                                            inputLayoutUserEmail.isErrorEnabled = true
                                            inputLayoutUserEmail.error = "Введите новую почту"
                                        } else {
                                            inputLayoutUserEmail.isErrorEnabled = false
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (intent.getIntExtra("LaunchReason", 0) == 1) {
                    inputLayoutUserEmail.visibility = View.GONE
                    inputLayoutUserPassword.visibility = View.VISIBLE
                    inputLayoutConfirmPassword.visibility = View.VISIBLE
                    textUpdateUser.text = getString(R.string.text_update_password)

                    layoutFirstWave.translationY = -575f
                    layoutSecondWave.translationY = -575f
                    inputLayoutUserPassword.translationX = 800f
                    inputLayoutConfirmPassword.translationX = 800f
                    buttonUpdateUser.translationX = 800f

                    inputLayoutUserPassword.alpha = 0f
                    inputLayoutConfirmPassword.alpha = 0f
                    buttonUpdateUser.alpha = 0f

                    layoutSecondWave.animate().translationY(0f).setDuration(500).setStartDelay(0).start()
                    layoutFirstWave.animate().translationY(0f).setDuration(500).setStartDelay(200).start()
                    inputLayoutUserPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                    inputLayoutConfirmPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                    buttonUpdateUser.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()

                    buttonUpdateUser.setOnClickListener {
                        vibrate()
                        if (isNetworkConnected()) {
                            if (inputUserPassword.text!!.isNotEmpty() && inputConfirmPassword.text!!.isNotEmpty()
                                && inputUserPassword.text!!.length >= 6
                                && inputUserPassword.text!!.toString() != sharedPreferences.getString("UserPassword", "")
                                && inputUserPassword.text!!.toString() == inputConfirmPassword.text!!.toString()) {
                                hideKeyboard()
                                firebaseAuth.currentUser!!.reauthenticate(
                                    EmailAuthProvider.getCredential(sharedPreferences.getString("UserEmail", "").toString(),
                                        sharedPreferences.getString("UserPassword", "").toString())).addOnCompleteListener { reauthTask ->
                                    if (reauthTask.isSuccessful) {
                                        firebaseAuth.currentUser!!.updatePassword(inputUserPassword.text!!.toString())
                                        editPreferences.putString("UserPassword", inputUserPassword.text.toString()).apply()
                                        Toast.makeText(baseContext, "Пароль пользователя обновлён!", Toast.LENGTH_SHORT).show()
                                        val activity = Intent(this@SignUpActivity, UserProfile::class.java)
                                        startActivity(activity)
                                        finish()
                                    } else {
                                        editPreferences.putBoolean("isUserLogged", false).apply()
                                        isUserLogged = false
                                        Toast.makeText(baseContext, "Не удалось переавторизироваться!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                if (inputUserPassword.text!!.isEmpty()) {
                                    inputLayoutUserPassword.isErrorEnabled = true
                                    inputLayoutUserPassword.error = "Введите пароль пользователя"
                                } else {
                                    if (inputUserPassword.text!!.length < 6) {
                                        inputLayoutUserPassword.isErrorEnabled = true
                                        inputLayoutUserPassword.error = "Пароль должен быть не меньше 6 символов"
                                    } else {
                                        if (inputUserPassword.text!!.toString() == sharedPreferences.getString("UserPassword", "")) {
                                            inputLayoutUserPassword.isErrorEnabled = true
                                            inputLayoutUserPassword.error = "Введите новый пароль"
                                        } else {
                                            inputLayoutUserPassword.isErrorEnabled = false
                                        }
                                    }
                                }
                                if (inputConfirmPassword.text!!.isEmpty()) {
                                    inputLayoutConfirmPassword.isErrorEnabled = true
                                    inputLayoutConfirmPassword.error = "Подтвердите пароль пользователя"
                                } else {
                                    if (inputConfirmPassword.text!!.toString() != inputUserPassword.text!!.toString()) {
                                        inputLayoutConfirmPassword.isErrorEnabled = true
                                        inputLayoutConfirmPassword.error = "Пароль не совпадает"
                                    } else {
                                        inputLayoutConfirmPassword.isErrorEnabled = false
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                layoutFirstWave.translationY = -575f
                layoutSecondWave.translationY = -575f
                inputLayoutUserEmail.translationX = 800f
                inputLayoutUserPassword.translationX = 800f
                inputLayoutConfirmPassword.translationX = 800f
                buttonCreateUser.translationX = 800f

                inputLayoutUserEmail.alpha = 0f
                inputLayoutUserPassword.alpha = 0f
                inputLayoutConfirmPassword.alpha = 0f
                buttonCreateUser.alpha = 0f

                layoutSecondWave.animate().translationY(0f).setDuration(500).setStartDelay(0).start()
                layoutFirstWave.animate().translationY(0f).setDuration(500).setStartDelay(200).start()
                inputLayoutUserEmail.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                inputLayoutUserPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                inputLayoutConfirmPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                buttonCreateUser.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()

                buttonCreateUser.setOnClickListener {
                    vibrate()
                    if (isNetworkConnected()) {
                        if (inputUserEmail.text!!.isNotEmpty() && inputUserPassword.text!!.isNotEmpty()
                            && inputConfirmPassword.text!!.isNotEmpty() && isValidEmail(inputUserEmail.text.toString())
                            && inputUserPassword.text!!.length >= 6 && inputConfirmPassword.text!!.toString() == inputUserPassword.text!!.toString()) {
                            hideKeyboard()
                            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this@SignUpActivity)
                            val loadingDialogBinding: LoadingDialogBinding = LoadingDialogBinding.inflate(layoutInflater)
                            loadingDialogBinding.textProgress.text = "Создание пользователя ..."
                            alertDialogBuilder.setView(loadingDialogBinding.root)
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.setCanceledOnTouchOutside(false)
                            alertDialog.show()

                            firebaseAuth.createUserWithEmailAndPassword(
                                inputUserEmail.text!!.toString(), inputUserPassword.text!!.toString())
                                .addOnCompleteListener { signUpActivity ->
                                    alertDialog.cancel()
                                    if (signUpActivity.isSuccessful) {
                                        val activity = Intent(this@SignUpActivity, UserProfile::class.java)
                                        activity.putExtra("UserEmail", inputUserEmail.text!!.toString())
                                        activity.putExtra("UserPassword", inputUserPassword.text!!.toString())
                                        startActivity(activity)
                                    } else {
                                        inputLayoutUserEmail.isErrorEnabled = true
                                        inputLayoutUserEmail.error = "Эта почта уже существует"
                                    }
                                }
                        } else {
                            if (inputUserEmail.text!!.isEmpty()) {
                                inputLayoutUserEmail.isErrorEnabled = true
                                inputLayoutUserEmail.error = "Введите почту пользователя"
                            } else {
                                if (!isValidEmail(inputUserEmail.text.toString())) {
                                    inputLayoutUserEmail.isErrorEnabled = true
                                    inputLayoutUserEmail.error = "Неправильная почта"
                                } else {
                                    inputLayoutUserEmail.isErrorEnabled = false
                                }
                            }
                            if (inputUserPassword.text!!.isEmpty()) {
                                inputLayoutUserPassword.isErrorEnabled = true
                                inputLayoutUserPassword.error = "Введите пароль пользователя"
                            } else {
                                if (inputUserPassword.text!!.length < 6) {
                                    inputLayoutUserPassword.isErrorEnabled = true
                                    inputLayoutUserPassword.error = "Пароль должен быть не меньше 6 символов"
                                } else {
                                    inputLayoutUserPassword.isErrorEnabled = false
                                }
                            }
                            if (inputConfirmPassword.text!!.isEmpty()) {
                                inputLayoutConfirmPassword.isErrorEnabled = true
                                inputLayoutConfirmPassword.error = "Подтвердите пароль пользователя"
                            } else {
                                if (inputConfirmPassword.text!!.toString() != inputUserPassword.text!!.toString()) {
                                    inputLayoutConfirmPassword.isErrorEnabled = true
                                    inputLayoutConfirmPassword.error = "Пароль не совпадает"
                                } else {
                                    inputLayoutConfirmPassword.isErrorEnabled = false
                                }
                            }
                        }
                    } else {
                        Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                    }
                }
            }

            buttonBack.setOnClickListener {
                vibrate()
                onBackPressedDispatcher.onBackPressed()
                finish()
            }
        }
    }

    private fun isValidEmail(inputText: CharSequence) : Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(inputText).matches()
    }

    private fun hideKeyboard() {
        this.currentFocus?.let { view ->
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @Suppress("DEPRECATION")
    private fun isNetworkConnected() : Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
                binding.buttonCreateUser.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
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