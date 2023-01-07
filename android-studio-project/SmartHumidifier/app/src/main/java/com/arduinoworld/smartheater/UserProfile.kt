package com.arduinoworld.smartheater

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.arduinoworld.smartheater.MainActivity.Companion.editPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.firebaseAuth
import com.arduinoworld.smartheater.MainActivity.Companion.isHapticFeedbackEnabled
import com.arduinoworld.smartheater.MainActivity.Companion.isUserLogged
import com.arduinoworld.smartheater.MainActivity.Companion.realtimeDatabase
import com.arduinoworld.smartheater.MainActivity.Companion.sharedPreferences
import com.arduinoworld.smartheater.MainActivity.Companion.vibrator
import com.arduinoworld.smartheater.databinding.ActivityUserProfileBinding
import com.arduinoworld.smartheater.databinding.LoadingDialogBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class UserProfile : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            setSupportActionBar(toolbarUserProfile)
            inputUserEmail.setText(sharedPreferences.getString("UserEmail", ""))
            inputUserPassword.setText(sharedPreferences.getString("UserPassword", ""))

            if (isUserLogged) {
                supportActionBar!!.title = getString(R.string.toolbar_user_profile)

                textUserEmail.text = inputUserEmail.text.toString()
                textUserPassword.text = inputUserPassword.text.toString()

                inputLayoutUserEmail.visibility = View.GONE
                inputLayoutUserPassword.visibility = View.GONE
                layoutResetPassword.visibility = View.GONE
                buttonLogin.visibility = View.GONE

                toolbarUserProfile.visibility = View.VISIBLE
                imageUserProfile.visibility = View.VISIBLE
                cardViewUserEmail.visibility = View.VISIBLE
                cardViewUserPassword.visibility = View.VISIBLE
                layoutUser.visibility = View.VISIBLE

                imageUserProfile.translationX = 800f
                cardViewUserEmail.translationX = 800f
                cardViewUserPassword.translationX = 800f
                layoutUser.translationX = 800f

                imageUserProfile.alpha = 0f
                cardViewUserEmail.alpha = 0f
                cardViewUserPassword.alpha = 0f
                layoutUser.alpha = 0f
                fabCreateUser.alpha = 0f

                imageUserProfile.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                cardViewUserEmail.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                cardViewUserPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                layoutUser.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
                fabCreateUser.animate().alpha(1f).setDuration(500).setStartDelay(400).start()
            } else {
                supportActionBar!!.title = getString(R.string.button_sign_in)
                if (intent.hasExtra("UserEmail") && intent.hasExtra("UserPassword")) {
                    val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this@UserProfile)
                    alertDialogBuilder.setTitle("Вход в пользователя")
                    alertDialogBuilder.setMessage("Войти в пользователя ${intent.getStringExtra("UserEmail")}?")
                    alertDialogBuilder.setPositiveButton("Продолжить") { _, _ ->
                        vibrate()
                        inputUserEmail.setText(intent.getStringExtra("UserEmail")!!)
                        inputUserPassword.setText(intent.getStringExtra("UserPassword")!!)
                        signIn(intent.getStringExtra("UserEmail")!!, intent.getStringExtra("UserPassword")!!)
                    }
                    alertDialogBuilder.setNegativeButton("Отмена") { _, _ ->
                        vibrate()
                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                }

                inputLayoutUserEmail.translationX = 800f
                inputLayoutUserPassword.translationX = 800f
                buttonResetPassword.translationX = 800f
                buttonLogin.translationX = 800f

                inputLayoutUserEmail.alpha = 0f
                inputLayoutUserPassword.alpha = 0f
                buttonResetPassword.alpha = 0f
                buttonLogin.alpha = 0f
                fabCreateUser.alpha = 0f

                inputLayoutUserEmail.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                inputLayoutUserPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                buttonResetPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                buttonLogin.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
                fabCreateUser.animate().alpha(1f).setDuration(500).setStartDelay(300).start()
            }

            buttonLogin.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    if (inputUserEmail.text!!.isNotEmpty() && inputUserPassword.text!!.isNotEmpty()) {
                        hideKeyboard()
                        signIn(inputUserEmail.text!!.toString(), inputUserPassword.text!!.toString())
                    } else {
                        if (inputUserEmail.text!!.isEmpty() && inputUserPassword.text!!.isNotEmpty()) {
                            inputLayoutUserEmail.isErrorEnabled = true
                            inputLayoutUserPassword.isErrorEnabled = false
                            inputLayoutUserEmail.error = "Введите почту пользователя"
                        } else if (inputUserEmail.text!!.isNotEmpty() && inputUserPassword.text!!.isEmpty()) {
                            inputLayoutUserEmail.isErrorEnabled = false
                            inputLayoutUserPassword.isErrorEnabled = true
                            inputLayoutUserPassword.error = "Введите пароль пользователя"
                        } else if (inputUserEmail.text!!.isEmpty() && inputUserPassword.text!!.isEmpty()) {
                            inputLayoutUserEmail.isErrorEnabled = true
                            inputLayoutUserPassword.isErrorEnabled = true
                            inputLayoutUserEmail.error = "Введите почту пользователя"
                            inputLayoutUserPassword.error = "Введите пароль пользователя"
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonResetPassword.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    if (inputUserEmail.text!!.isNotEmpty()) {
                        if (isValidEmail(inputUserEmail.text.toString())) {
                            hideKeyboard()
                            val alertDialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this@UserProfile)
                            alertDialogBuilder.setTitle("Сброс пароля")
                            alertDialogBuilder.setMessage("Отправить письмо для сброса пароля на почту ${binding.inputUserEmail.text}?")
                            alertDialogBuilder.setPositiveButton("Продолжить") { _, _ ->
                                vibrate()
                                firebaseAuth.sendPasswordResetEmail(binding.inputUserEmail.text.toString()).addOnCompleteListener(this@UserProfile) { resetPasswordTask ->
                                    if (resetPasswordTask.isSuccessful) {
                                        Toast.makeText(baseContext, "Письмо для сброса\nпароля отправлено!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(baseContext, "Не удалось отправить\nписьмо для сброса пароля!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            alertDialogBuilder.setNegativeButton("Отмена") { _, _ ->
                                vibrate()
                            }
                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        } else {
                            inputLayoutUserEmail.isErrorEnabled = true
                            inputLayoutUserPassword.isErrorEnabled = false
                            inputLayoutUserEmail.error = "Неправильная почта"
                        }
                    } else {
                        inputLayoutUserEmail.isErrorEnabled = true
                        inputLayoutUserPassword.isErrorEnabled = false
                        inputLayoutUserEmail.error = "Введите почту пользователя"
                    }
                } else {
                    Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonLogout.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    imageUserProfile.translationX = 0f
                    cardViewUserEmail.translationX = 0f
                    cardViewUserPassword.translationX = 0f
                    layoutUser.translationX = 0f
                    hideUserProfile()
                    intent.removeExtra("UserEmail")
                    intent.removeExtra("UserPassword")
                    editPreferences.putBoolean("isUserLogged", false).apply()
                    isUserLogged = false
                    firebaseAuth.signOut()
                } else {
                    Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonDelete.setOnClickListener {
                vibrate()
                if (isNetworkConnected()) {
                    val alertDialogDeleteUserBuilder = androidx.appcompat.app.AlertDialog.Builder(this@UserProfile)
                    alertDialogDeleteUserBuilder.setTitle("Удаление пользователя")
                    alertDialogDeleteUserBuilder.setMessage("Вы точно хотите удалить пользователя ${inputUserEmail.text.toString()}?")
                    alertDialogDeleteUserBuilder.setPositiveButton("Подтвердить") { _, _ ->
                        vibrate()
                        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this@UserProfile)
                        val loadingDialogBinding: LoadingDialogBinding = LoadingDialogBinding.inflate(layoutInflater)
                        loadingDialogBinding.textProgress.text = "Удаление пользователя ..."
                        alertDialogBuilder.setView(loadingDialogBinding.root)
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.setCanceledOnTouchOutside(false)
                        alertDialog.show()

                        val uid = firebaseAuth.currentUser!!.uid
                        isUserLogged = false

                        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted").removeValue()
                        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("temperature").removeValue()
                        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidity").removeValue()
                        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidityRange").removeValue()
                        realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidifierOnOffTime").removeValue()
                            .addOnCompleteListener {
                                firebaseAuth.currentUser!!.reauthenticate(
                                    EmailAuthProvider.getCredential(binding.inputUserEmail.text.toString(),
                                        binding.inputUserPassword.text.toString())).addOnCompleteListener { reauthTask ->
                                    if (reauthTask.isSuccessful) {
                                        firebaseAuth.currentUser!!.delete().addOnCompleteListener { deleteUserTask ->
                                            if (deleteUserTask.isSuccessful) {
                                                binding.inputUserEmail.setText("")
                                                binding.inputUserPassword.setText("")
                                                editPreferences.putString("UserEmail", "")
                                                editPreferences.putString("UserPassword", "")
                                                editPreferences.putBoolean(uid, false)
                                                Toast.makeText(baseContext, "Пользователь удалён!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(baseContext, "Не удалось удалить пользователя!", Toast.LENGTH_LONG).show()
                                            }
                                            alertDialog.cancel()
                                            hideUserProfile()
                                            editPreferences.putBoolean("isUserLogged", false).apply()
                                        }
                                    } else {
                                        alertDialog.cancel()
                                        hideUserProfile()
                                        editPreferences.putBoolean("isUserLogged", false).apply()
                                        Toast.makeText(baseContext, "Не удалось переавторизироваться!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    }
                    alertDialogDeleteUserBuilder.setNegativeButton("Отмена") { _, _ ->
                        vibrate()
                    }
                    val alertDialogDeleteUser = alertDialogDeleteUserBuilder.create()
                    alertDialogDeleteUser.show()
                } else {
                    Toast.makeText(baseContext, "Нет подключения\nк Интернету!", Toast.LENGTH_LONG).show()
                }
            }

            buttonEditEmail.setOnClickListener {
                vibrate()
                val activity = Intent(this@UserProfile, SignUpActivity::class.java)
                activity.putExtra("LaunchReason", 0)
                startActivity(activity)
            }

            buttonEditPassword.setOnClickListener {
                vibrate()
                val activity = Intent(this@UserProfile, SignUpActivity::class.java)
                activity.putExtra("LaunchReason", 1)
                startActivity(activity)
            }

            fabCreateUser.setOnClickListener {
                vibrate()
                val activity = Intent(this@UserProfile, SignUpActivity::class.java)
                startActivity(activity)
            }
        }
    }

    private fun signIn(userEmail: String, userPassword: String) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this@UserProfile)
        val loadingDialogBinding: LoadingDialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        loadingDialogBinding.textProgress.text = "Вход в пользователя ..."
        alertDialogBuilder.setView(loadingDialogBinding.root)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

        var isAnimationStarted = false
        with(binding) {
            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        alertDialog.cancel()

                        inputLayoutUserEmail.translationX = 0f
                        inputLayoutUserPassword.translationX = 0f
                        buttonResetPassword.translationX = 0f
                        buttonLogin.translationX = 0f

                        inputLayoutUserEmail.isErrorEnabled = false
                        inputLayoutUserPassword.isErrorEnabled = false

                        inputLayoutUserEmail.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0).start()
                        inputLayoutUserPassword.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(100).start()
                        buttonResetPassword.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(200).start()
                        buttonLogin.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(300)
                            .setListener(object: Animator.AnimatorListener {
                                override fun onAnimationStart(p0: Animator) {}

                                override fun onAnimationEnd(p0: Animator) {
                                    if (!isAnimationStarted) {
                                        isAnimationStarted = true
                                        supportActionBar!!.title = getString(R.string.toolbar_user_profile)

                                        textUserEmail.text = inputUserEmail.text.toString()
                                        textUserPassword.text = inputUserPassword.text.toString()

                                        inputLayoutUserEmail.visibility = View.GONE
                                        inputLayoutUserPassword.visibility = View.GONE
                                        layoutResetPassword.visibility = View.GONE
                                        buttonLogin.visibility = View.GONE

                                        imageUserProfile.visibility = View.VISIBLE
                                        cardViewUserEmail.visibility = View.VISIBLE
                                        cardViewUserPassword.visibility = View.VISIBLE
                                        layoutUser.visibility = View.VISIBLE

                                        imageUserProfile.translationX = 800f
                                        cardViewUserEmail.translationX = 800f
                                        cardViewUserPassword.translationX = 800f
                                        layoutUser.translationX = 800f

                                        imageUserProfile.alpha = 0f
                                        cardViewUserEmail.alpha = 0f
                                        cardViewUserPassword.alpha = 0f
                                        layoutUser.alpha = 0f

                                        imageUserProfile.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                                        cardViewUserEmail.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                                        cardViewUserPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                                        layoutUser.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
                                    }
                                }

                                override fun onAnimationCancel(p0: Animator) {}
                                override fun onAnimationRepeat(p0: Animator) {}

                            }).start()

                        if (!sharedPreferences.getBoolean(firebaseAuth.currentUser!!.uid, false)) {
                            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("isHumidifierStarted").setValue(false)
                            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("temperature").setValue(0)
                            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidity").setValue(0)
                            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidityRange").setValue(" ")
                            realtimeDatabase.child(firebaseAuth.currentUser!!.uid).child("humidifierOnOffTime").setValue(" ")
                            editPreferences.putBoolean(firebaseAuth.currentUser!!.uid, true)
                        }
                        editPreferences.putString("UserEmail", userEmail)
                        editPreferences.putString("UserPassword", userPassword)
                        editPreferences.putBoolean("isUserLogged", true).apply()
                        isUserLogged = true
                    } else {
                        alertDialog.cancel()
                        try {
                            throw signInTask.exception!!
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            inputLayoutUserEmail.isErrorEnabled = false
                            inputLayoutUserPassword.isErrorEnabled = true
                            inputLayoutUserPassword.error = "Неверный пароль"
                        } catch (e: FirebaseAuthInvalidUserException) {
                            when (e.errorCode) {
                                "ERROR_USER_NOT_FOUND" -> {
                                    inputLayoutUserPassword.isErrorEnabled = false
                                    inputLayoutUserEmail.isErrorEnabled = true
                                    inputLayoutUserEmail.error = "Почта не обнаружена"
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun hideUserProfile() {
        with(binding) {
            var isAnimationStarted = false
            imageUserProfile.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(0).start()
            cardViewUserEmail.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(100).start()
            cardViewUserPassword.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(200).start()
            layoutUser.animate().translationX(-800f).alpha(0f).setDuration(500).setStartDelay(300)
                .setListener(object: Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {}

                    override fun onAnimationEnd(p0: Animator) {
                        if (!isAnimationStarted) {
                            isAnimationStarted = true
                            supportActionBar!!.title = "Вход"

                            imageUserProfile.visibility = View.GONE
                            cardViewUserEmail.visibility = View.GONE
                            cardViewUserPassword.visibility = View.GONE
                            layoutUser.visibility = View.GONE

                            inputLayoutUserEmail.visibility = View.VISIBLE
                            inputLayoutUserPassword.visibility = View.VISIBLE
                            layoutResetPassword.visibility = View.VISIBLE
                            buttonLogin.visibility = View.VISIBLE

                            inputLayoutUserEmail.translationX = 800f
                            inputLayoutUserPassword.translationX = 800f
                            buttonResetPassword.translationX = 800f
                            buttonLogin.translationX = 800f

                            inputLayoutUserEmail.alpha = 0f
                            inputLayoutUserPassword.alpha = 0f
                            buttonResetPassword.alpha = 0f
                            buttonLogin.alpha = 0f

                            inputLayoutUserEmail.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(0).start()
                            inputLayoutUserPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(100).start()
                            buttonResetPassword.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(200).start()
                            buttonLogin.animate().translationX(0f).alpha(1f).setDuration(500).setStartDelay(300).start()
                        }
                    }

                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationRepeat(p0: Animator) {}
                }).start()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val actionButtonsInflater = menuInflater
        actionButtonsInflater.inflate(R.menu.user_profile_menu, menu)
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
                binding.fabCreateUser.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
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