package com.example.chatlication.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import com.example.chatlication.R
import com.example.chatlication.databinding.ActivitySignupBinding
import com.example.chatlication.utils.Constants
import com.example.chatlication.utils.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.handleCoroutineException
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class   Signup : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var encodedImage: String
    private lateinit var preferenceManager:PreferenceManager

    private lateinit var pickImage:ActivityResultLauncher<Intent>

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        preferenceManager = PreferenceManager(applicationContext)
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater
        )
        encodedImage = ""
        pickImage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            res -> if(res.resultCode == RESULT_OK){
                if(res.data !=null){
                    val imageUri = res.data!!.data
                    try{
                        val inputStream = imageUri?.let { contentResolver.openInputStream(it) }
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE
                        encodedImage = encodeImage(bitmap)


                    }catch(e:FileNotFoundException){
                        e.printStackTrace()

                    }
                }
        }
        }
        enableEdgeToEdge()
        setContentView(binding.root)
        setListeners()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    private fun setListeners(){
        binding.textSignIn.setOnClickListener{
            val intent = Intent(this,Signin::class.java)
            startActivity(intent)
        }
        binding.buttonSignin.setOnClickListener{
            if(isValidCredentials()){
                signUp()
            }
        }
        binding.imageProfile.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)

        }
    }
    private fun isLoading(isLoading:Boolean){
        if(isLoading){
            binding.buttonSignin.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
        else{
            binding.buttonSignin.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE

        }
    }
    private fun showMessage(message: String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }
    private fun signUp(){
        isLoading(true)
        val database = FirebaseFirestore.getInstance()
        val user = HashMap<String,Any>()
        user[Constants.KEY_NAME] = binding.inputName.text.toString()
        user[Constants.KEY_EMAIL] = binding.inputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding.inputPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage

        database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener {
            isLoading(false)
            showMessage("message sent")
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true)
            preferenceManager.putString(Constants.KEY_USER_ID,it.id)
            preferenceManager.putString(Constants.KEY_NAME,binding.inputName.text.toString())
            preferenceManager.putString(Constants.KEY_IMAGE,encodedImage)
            val intent = Intent(applicationContext,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK )
            startActivity(intent)


        }.addOnFailureListener{
            isLoading(false)
            showMessage(it.message.toString())

        }
    }
    private fun encodeImage(bitmap:Bitmap) :String{
        val prevWidth = 150
        val prevHeight = bitmap.height*prevWidth/bitmap.width
        val prevBitmap = Bitmap.createScaledBitmap(bitmap,prevWidth,prevHeight,false)
        val byteArrayStream = ByteArrayOutputStream()
        prevBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayStream)
        val bytes = byteArrayStream.toByteArray()
        return Base64.encodeToString(bytes,Base64.DEFAULT)
        
    }


    private fun isValidCredentials():Boolean{
        if(encodedImage == null){
            showMessage("please select profile image")
            return false
        }
        if(binding.inputName.text.toString().trim().isEmpty()){
            showMessage("please enter name")
            return false
        }
        if(binding.inputEmail.text.toString().isEmpty()){
            showMessage("please enter email")
            return false
        }
        if(binding.inputPassword.text.toString().isEmpty()){
            showMessage("please enter password")
            return false
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text).matches()){
            showMessage("enter valid email")
            return false
        }
        if(binding.inputConfirmPassword.text.isEmpty()){
            showMessage("confirm password")
            return false
        }
        if(binding.inputPassword.text.equals(binding.inputConfirmPassword.text)){
            showMessage("passwords doesn't match")
            return false
        }
        if(encodedImage.isEmpty()){
            showMessage("image cannot be empty")
            return false
        }
        return true


    }
}