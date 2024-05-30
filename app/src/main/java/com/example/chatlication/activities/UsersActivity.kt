package com.example.chatlication.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.example.chatlication.R
import com.example.chatlication.adapters.UserAdapter
import com.example.chatlication.databinding.ActivityUsersBinding
import com.example.chatlication.listeners.UserListener
import com.example.chatlication.models.UserModel
import com.example.chatlication.utils.Constants
import com.example.chatlication.utils.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : AppCompatActivity(),UserListener {
    lateinit var binding:ActivityUsersBinding
    lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(applicationContext)
        setContentView(binding.root )
        setListeners()
        getUsers()
    }
    private fun setListeners(){
        binding.imageBack.setOnClickListener{
            v-> onBackPressed()

        }
    }

    private fun loading(isloading:Boolean){
        if(isloading){
            binding.usersProgressbar.visibility = View.VISIBLE
        }
        else{
            binding.usersProgressbar.visibility = View.INVISIBLE
        }
    }
    private fun showErrorMessage(){
        binding.errorMessage.text = String.format("%s","No user Available")
        binding.errorMessage.visibility = View.VISIBLE
    }
    private fun getUsers(){
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS).get().addOnCompleteListener{
            loading(false)
            val curruserID= preferenceManager.getString(Constants.KEY_USER_ID)
            if( it.isSuccessful && it.result != null){
                val users = ArrayList<UserModel>()
                for (doc in it.result){
                    if(curruserID.equals(doc.id)){
                        continue
                    }
                    println(doc.id)
                    val user = UserModel(
                        doc.getString(Constants.KEY_NAME).toString(),
                        doc.getString(Constants.KEY_IMAGE).toString(),
                        doc.getString(Constants.KEY_EMAIL).toString(),
                        doc.getString(Constants.KEY_FCM_TOKEN).toString(),
                        doc.id
                    )
                    users.add(user)

                }
                if(users.size>0){
                    val userAdapter = UserAdapter(users,this)
                    binding.usersRecyclerView.adapter = userAdapter
                    binding.usersRecyclerView.visibility = View.VISIBLE

                }
                else{
                    showErrorMessage()
                }
            }else{
                showErrorMessage()
            }
        }
    }

    override fun onUserClicked(user: UserModel) {
        val intent = Intent(applicationContext,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
        finish()

    }
}