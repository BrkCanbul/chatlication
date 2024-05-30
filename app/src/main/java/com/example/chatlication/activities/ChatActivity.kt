package com.example.chatlication.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
//import com.example.chatlication.R
import com.example.chatlication.adapters.ChatAdapter
import com.example.chatlication.databinding.ActivityChatBinding
import com.example.chatlication.models.ChatMessage
import com.example.chatlication.models.UserModel
import com.example.chatlication.utils.Constants
import com.example.chatlication.utils.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.ArrayList

import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChatBinding
    private lateinit var recieverUser:UserModel
    private lateinit var chatMessages:ArrayList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database:FirebaseFirestore
    private var conversionId :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }
    private fun init(){
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList<ChatMessage>()

        val userid = preferenceManager.getString(Constants.KEY_USER_ID).toString()
        if(userid.isEmpty()){
            return
        }
        val profileImage = getBitmapEncodedString(recieverUser.image.toString())
        chatAdapter = ChatAdapter(chatMessages,userid,profileImage)
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()

    }

    private fun sendMessage(){
        val messageObj = HashMap<String,Any?>()
        messageObj[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        messageObj[Constants.KEY_RECEIVER_ID] = recieverUser.id
        messageObj[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        messageObj[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION_CHAT).add(messageObj)
        if(conversionId != null){
                updateConversion(binding.inputMessage.text.toString())
        }else{
            val conversion = HashMap<String,Any>()
            conversion[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID).toString()
            conversion[Constants.KEY_SENDER_NAME] = preferenceManager.getString(Constants.KEY_NAME).toString()
            conversion[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE).toString()
            conversion[Constants.KEY_RECEIVER_ID] = recieverUser.id.toString()
            conversion[Constants.KEY_RECEIVER_NAME] = recieverUser.name.toString()
            conversion[Constants.KEY_RECEIVER_IMAGE] = recieverUser.image.toString()
            conversion[Constants.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversion[Constants.KEY_TIMESTAMP] = Date()
            addConversion(conversion)
        }
        binding.inputMessage.text = null

    }
    private fun getBitmapEncodedString(encodedImg:String):Bitmap{
        val bytes = Base64.decode(encodedImg,Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }
    private fun listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID,recieverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID,recieverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }
    @SuppressLint("NotifyDataSetChanged")
    private val  eventListener = EventListener<QuerySnapshot> { value, error ->
        if(error != null){
            return@EventListener
        }
        if(value != null){

            for(docChange in value.documentChanges){
                if(docChange.type == DocumentChange.Type.ADDED){
                    val senderId = docChange.document.getString(Constants.KEY_SENDER_ID).toString()
                    val receiverId =docChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                    val message =docChange.document.getString(Constants.KEY_MESSAGE).toString()
                    val dateTime = docChange.document.getDate(Constants.KEY_TIMESTAMP)
                        ?.let { getDateTime(it) }.toString()
                    var dateObj = docChange.document.getDate(Constants.KEY_TIMESTAMP)
                    if(dateObj == null){
                        dateObj = Date(1)

                    }
                    val chatMessage =  ChatMessage(
                        senderId = senderId,
                        receiverId = receiverId,
                        message= message,
                        dateTime = dateTime,
                        dateObj = dateObj
                    )
                    chatMessages.add(chatMessage)

                }
            }
            val count = chatMessages.size
            chatMessages.sortWith { val1, val2 -> val1.dateObj?.compareTo(val2.dateObj)!! }
            if(count==0){
                chatAdapter.notifyDataSetChanged()
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessages.size,chatMessages.size)
               binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size-1)

            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
        if(conversionId == null){
            checkForConversion()
        }
    }
    private fun addConversion(conversion:HashMap<String,Any>){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener { conversionId= it.id }
    }

    private fun updateConversion(message:String){
    val docrefer = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId!!)
        docrefer.update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,Date())
    }
    private fun getDateTime(date:Date):String{
        return SimpleDateFormat("MMMM dd, yyyy\nhh:mm", Locale.getDefault()).format(date)

    }

    private fun loadReceiverDetails(){
        recieverUser = intent.getSerializableExtra(Constants.KEY_USER) as UserModel
        binding.textName.text = recieverUser.name


    }
    private fun setListeners(){
        binding.imageBack.setOnClickListener{onBackPressed()}
        binding.layoutSend.setOnClickListener{sendMessage()}
    }

    private fun checkForConversion(){
        if(chatMessages.size != 0){
            checkConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID).toString(),
                recieverUser.id.toString()
            )
            checkConversionRemotely(
                recieverUser.id.toString(),
                preferenceManager.getString(Constants.KEY_USER_ID).toString()
            )

        }

    }

    private fun checkConversionRemotely(senderId:String,receiverId:String){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }
    private  val conversionOnCompleteListener = OnCompleteListener<QuerySnapshot>{ task ->
        if(task.isSuccessful && task.result != null && task.result.documents.size>0){
            val docSnapshot = task.result.documents[0]
            conversionId = docSnapshot.id
        }

    }
}