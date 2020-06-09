package com.iew.fun2order

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_logon_sms_verify_code.*
import java.util.concurrent.TimeUnit

class VerifyCode: AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private var mVerificationPhoneNo:String? = null
    private var mVerificationId:String? = null

    private var mVerificationInProgress = false
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logon_sms_verify_code)

        supportActionBar?.hide()

        val bundle = intent.extras
        mVerificationPhoneNo = bundle!!.getString("VerifyPhoneNo")

        mAuth = FirebaseAuth.getInstance()

        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                mVerificationInProgress = false
                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                Log.w(TAG, "onVerificationFailed", e)
                mVerificationInProgress = false
                if (e is FirebaseAuthInvalidCredentialsException) {
                    val notifyAlert = AlertDialog.Builder(this@VerifyCode).create()
                    notifyAlert.setTitle("認證錯誤")
                    notifyAlert.setMessage("詳細訊息: \n${e.localizedMessage}")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                           finish()
                    }
                    notifyAlert.show()

                } else if (e is FirebaseTooManyRequestsException) {

                    val notifyAlert = AlertDialog.Builder(this@VerifyCode).create()
                    notifyAlert.setTitle("認證錯誤")
                    notifyAlert.setMessage("登入太頻繁請稍後再測試\n詳細訊息 :\n ${e.localizedMessage}")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                        finish()
                    }
                    notifyAlert.show()
                }
                updateUI(STATE_VERIFY_FAILED);
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent:$verificationId")
                mVerificationId = verificationId
                mResendToken = token
                updateUI(STATE_CODE_SENT);
            }
        }

        btnSendCode.setOnClickListener{
            val code = editTextVerifyCode.text.toString()
            if (TextUtils.isEmpty(code))
            {
                editTextVerifyCode.error = "Cannot be empty."
            }
            else{
                verifyPhoneNumberWithCode(mVerificationId, code)
            }
        }

        txtReSendCode.setOnClickListener{
            if(mVerificationPhoneNo != null) {
                startPhoneNumberVerification(mVerificationPhoneNo!!)
            }
        }


        if(mVerificationPhoneNo != null) {
            startPhoneNumberVerification(mVerificationPhoneNo!!)
        }
    }

    private fun startPhoneNumberVerification(phoneNumber:String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            mCallbacks!!)        // OnVerificationStateChangedCallbacks
            mVerificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId:String?, code:String) {
        try
        {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }
        catch (e:Exception) {
            val notifyAlert = AlertDialog.Builder(this@VerifyCode).create()
            notifyAlert.setTitle("認證錯誤")
            notifyAlert.setMessage("詳細訊息 :\n ${e.localizedMessage}")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                finish()
            }
            notifyAlert.show()
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result!!.user
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                } else {

                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException)
                    {
                        editTextVerifyCode.error = "Invalid code."
                    }
                    updateUI(STATE_SIGNIN_FAILED)
                }
            }
    }

    private fun signOut() {
        mAuth!!.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null)
        {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        }
        else
        {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int) {
        updateUI(uiState, mAuth!!.currentUser, null)
    }

    private fun updateUI(uiState: Int, user: FirebaseUser) {
        updateUI(uiState, user, null)
    }

    private fun updateUI(uiState:Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(uiState:Int, user: FirebaseUser? = mAuth!!.currentUser, cred: PhoneAuthCredential? = null) {

        when (uiState) {

            STATE_INITIALIZED ->
                Toast.makeText(this, R.string.status_initial, Toast.LENGTH_SHORT).show()
            STATE_CODE_SENT ->
                Toast.makeText(this, R.string.status_code_sent, Toast.LENGTH_SHORT).show()
            STATE_VERIFY_FAILED ->
                Toast.makeText(this, R.string.status_verification_failed, Toast.LENGTH_LONG).show()
            STATE_VERIFY_SUCCESS -> {
                Toast.makeText(this, R.string.status_verification_succeeded, Toast.LENGTH_SHORT).show()
                if (cred != null)
                {
                    if (cred.smsCode != null)
                    {
                        Toast.makeText(this, cred.getSmsCode(), Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        Toast.makeText(this, R.string.instant_validation, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            STATE_SIGNIN_FAILED -> {
                Toast.makeText(this, R.string.status_sign_in_failed, Toast.LENGTH_LONG).show()
                val notifyAlert = AlertDialog.Builder(this@VerifyCode).create()
                notifyAlert.setTitle("認證錯誤")
                notifyAlert.setMessage("詳細訊息:")
                notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                }
                notifyAlert.show()
            }
            STATE_SIGNIN_SUCCESS -> {
                Toast.makeText(this,R.string.status_sign_in_successful , Toast.LENGTH_LONG).show()
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setClass(this, MainActivity::class.java!!)
                startActivity(intent)
                this.finish()
            }
        }

        if (user != null)
        {
            val newIntent = Intent()
            newIntent.setClass(this, MainActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            newIntent.putExtras(Bundle())
            startActivity(newIntent)
            this.finish()
        }
    }

    companion object {
        private const val TAG = "Logon"
        private const val STATE_INITIALIZED = 1
        private const val STATE_CODE_SENT = 2
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}
