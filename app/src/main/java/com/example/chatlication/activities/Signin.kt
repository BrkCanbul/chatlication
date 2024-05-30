package com.example.chatlication.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatlication.R
import com.example.chatlication.databinding.ActivitySigninBinding
import com.example.chatlication.databinding.ActivitySignupBinding
import com.example.chatlication.utils.Constants
import com.example.chatlication.utils.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class Signin : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding;
    private lateinit var preferences: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySigninBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        preferences = PreferenceManager(applicationContext)
        if(preferences.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()

        }
        setListeners()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun isValidSignCredentials():Boolean{
        if(binding.inputEmail.text.isEmpty() or !Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text).matches()){
            showMessage("please enter email correctly")
            return false
        }
        if(binding.inputPassword.text.isEmpty()){
            showMessage("please enter password")
            return false
        }
        return true
    }
    private fun SignIn(){
        val database = FirebaseFirestore.getInstance()

        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener{
                if(it.isSuccessful && it.result!= null && it.result.documents.size>0){
                    val docsnapshot = it.result.documents.get(0)
                    preferences.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
                    preferences.putString(Constants.KEY_USER_ID,docsnapshot.id)
                    val name :String = docsnapshot.getString(Constants.KEY_NAME).toString()
                    val image :String = docsnapshot.getString(Constants.KEY_IMAGE).toString()
                    preferences.putString(Constants.KEY_NAME,name)
                    preferences.putString(Constants.KEY_IMAGE,image)
                    val intent = Intent(applicationContext,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else{
                    showMessage("cant sign in")
                }
            }.addOnFailureListener{
                showMessage(it.message.toString())

            }
    }

    private fun setListeners(){

        binding.textCreateAccount.setOnClickListener{
            val intent = Intent(this,Signup::class.java)
            startActivity(intent)
        }

        binding.buttonSignin.setOnClickListener{
            if(isValidSignCredentials()){
                SignIn()
            }


        }
    }
    private fun showMessage(message: String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }
}