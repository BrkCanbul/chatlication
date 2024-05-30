package com.example.chatlication.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatlication.databinding.ActivityChatBinding
import com.example.chatlication.databinding.ItemContainerRecievedMessageBinding
import com.example.chatlication.databinding.ItemContainerSentMessageBinding
import com.example.chatlication.models.ChatMessage
import kotlin.coroutines.coroutineContext

class ChatAdapter(chatMessages:List<ChatMessage>,senderId:String,receiverProfileImage: Bitmap)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var chatMessages:List<ChatMessage>
    var senderId:String
    var recieverProfileImage:Bitmap
    val VIEW_TYPE_SENT=1
    val VIEW_TYPE_RECIEVED=2

    init {
        this.chatMessages = chatMessages
        this.recieverProfileImage = receiverProfileImage
        this.senderId = senderId

    }
    class SentMessageViewHolder(itemContainerSentMessageBinding: ItemContainerSentMessageBinding)
        :RecyclerView.ViewHolder(itemContainerSentMessageBinding.root){
            var binding:ItemContainerSentMessageBinding = itemContainerSentMessageBinding;
        fun setData(message:ChatMessage){
            binding.textMessage.text =message.message
            binding.textDateTime.text = message.dateTime

        }
    }
    class RecievedMessageViewHolder(itemContainerRecievedMessageBinding: ItemContainerRecievedMessageBinding)
        :RecyclerView.ViewHolder(itemContainerRecievedMessageBinding.root){
            var binding = itemContainerRecievedMessageBinding
        fun setData(chatMessage:ChatMessage,recieverProfileImage:Bitmap){
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(recieverProfileImage)
        }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_SENT){
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )
        }
        return RecievedMessageViewHolder(
                ItemContainerRecievedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
    }

    override fun getItemCount(): Int {
       return chatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(chatMessages.get(position))

        }
        else {
            (holder as RecievedMessageViewHolder).setData(
                chatMessages.get(position),
                recieverProfileImage
            )
        }
        }

    override fun getItemViewType(position: Int): Int {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT
        }
        return VIEW_TYPE_RECIEVED
    }
}