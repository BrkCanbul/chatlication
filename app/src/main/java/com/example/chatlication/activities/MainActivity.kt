package com.example.chatlication.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.example.chatlication.databinding.ActivityMain2Binding
import com.example.chatlication.utils.Constants
import com.example.chatlication.utils.PreferenceManager
import android.util.Base64
import android.view.View
import android.widget.Toast

import com.example.chatlication.adapters.RecentConversationAdapter
import com.example.chatlication.listeners.ConversionListener
import com.example.chatlication.models.ChatMessage
import com.example.chatlication.models.UserModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Collections


class MainActivity : AppCompatActivity(),ConversionListener {

    private lateinit var binding:ActivityMain2Binding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations:MutableList<ChatMessage>
    private lateinit var recentConversationAdapter: RecentConversationAdapter
    private lateinit var database:FirebaseFirestore

    private fun init(){
        conversations = ArrayList()
        recentConversationAdapter = RecentConversationAdapter(conversations,this)
        binding.conversationsRecyclerView.adapter = recentConversationAdapter
        database = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()

        listenConversations()

    }
    private fun setListeners(){
        binding.imageSignout.setOnClickListener{
            signOut()
        }
        binding.fabNewChat.setOnClickListener{
            startActivity(Intent(applicationContext,UsersActivity::class.java))
        }
    }

    private fun loadUserDetails(){
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val encodedImage = preferenceManager.getString(Constants.KEY_IMAGE)
        val bytes:ByteArray = Base64.decode(encodedImage,Base64.DEFAULT)
        val bitmap:Bitmap =BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }
    private fun showMessage(message: String){
        Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
    }
    val  eventListener = EventListener<QuerySnapshot>{ value, error ->
        if(error != null){
            return@EventListener
        }
        if(value != null){
            for (docChange in value.documentChanges){
                if (docChange.type == DocumentChange.Type.ADDED){
                    val senderId = docChange.document.getString(Constants.KEY_SENDER_ID)
                    val receiverId = docChange.document.getString(Constants.KEY_RECEIVER_ID)
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = senderId
                    chatMessage.receiverId= receiverId
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = docChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                        chatMessage.conversionName  = docChange.document.getString(Constants.KEY_RECEIVER_NAME)
                        chatMessage.conversionId    = docChange.document.getString(Constants.KEY_RECEIVER_ID)
                    }
                    else{
                        chatMessage.conversionImage =docChange.document.getString(Constants.KEY_SENDER_IMAGE)
                        chatMessage.conversionName  =docChange.document.getString(Constants.KEY_SENDER_NAME)
                        chatMessage.conversionId    =docChange.document.getString(Constants.KEY_SENDER_ID)
                    }
                    chatMessage.message = docChange.document.getString(Constants.KEY_LAST_MESSAGE)
                    chatMessage.dateObj = docChange.document.getDate(Constants.KEY_TIMESTAMP)
                    conversations.add(chatMessage)



                }else if(docChange.type ==  DocumentChange.Type.MODIFIED){
                    for(i in conversations.indices){
                        val senderId = docChange.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId = docChange.document.getString(Constants.KEY_RECEIVER_ID)
                        if(conversations[i].senderId.equals(senderId) && conversations[i].receiverId.equals(receiverId)){
                            conversations[i].message = docChange.document.getString(Constants.KEY_LAST_MESSAGE)
                            conversations[i].dateObj = docChange.document.getDate(Constants.KEY_TIMESTAMP)
                            break
                        }
                    }

                }
            }
            Collections.sort(conversations,{obj1,obj2->obj2.dateObj!!.compareTo(obj1.dateObj)} )

            recentConversationAdapter.notifyDataSetChanged()
            binding.conversationsRecyclerView.smoothScrollToPosition(0)
            binding.progressBar.visibility = View.GONE
            binding.conversationsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun listenConversations(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }
    private fun updateToken(token:String){
        val database = FirebaseFirestore.getInstance()
        val docrefer = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USERS).document(
                it
            )
        }
        docrefer?.update(Constants.KEY_FCM_TOKEN,token)?.addOnSuccessListener { showMessage("Token Updated Successfull") }
            ?.addOnFailureListener{ showMessage("token update failed")}
    }
    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)


    }
    private fun signOut(){
        showMessage("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val docrefer = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USERS).document(
                it
            )
        }
        val hashMap = HashMap<String,Any>()
        hashMap[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        docrefer?.update(hashMap)?.addOnSuccessListener {
            preferenceManager.clear()
            startActivity(Intent(applicationContext,Signin::class.java))
            finish()
        }?.addOnFailureListener{
            showMessage("unable to sign out")
        }
    }

    override fun onConversionClicked(user: UserModel) {
        val intent = Intent(applicationContext,ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER,user)
        startActivity(intent)
    }
}