package com.example.chatlication.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatlication.databinding.ItemRecentConversationsBinding
import com.example.chatlication.listeners.ConversionListener
import com.example.chatlication.models.ChatMessage
import com.example.chatlication.models.UserModel



class RecentConversationAdapter(private val chatMessages: List<ChatMessage>,
                                private val conversionListener: ConversionListener)
    : RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>() {

    companion object{
        var conversionListener:ConversionListener? = null
        fun getConversionImage(image:String):Bitmap{
            val img = Base64.decode(image, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(
                img,
                0
                ,img.size
            )
        }

    }
    init {
        Companion.conversionListener = this.conversionListener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemRecentConversationsBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false)
        )
    }

    override fun getItemCount(): Int {
        for(i in chatMessages){
            println(i)
        }
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    class ConversionViewHolder(private val itemBinding:ItemRecentConversationsBinding)
        :RecyclerView.ViewHolder(itemBinding.root){



        fun setData(chatMessage: ChatMessage){

            itemBinding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage!!))

            itemBinding.textName.text = chatMessage.conversionName
            itemBinding.textRecentMessage.text = chatMessage.message
            itemBinding.root.setOnClickListener{
                val user = UserModel(
                    id = chatMessage.conversionId,
                    name = chatMessage.conversionName,
                    image = chatMessage.conversionImage
                )
                Companion.conversionListener?.onConversionClicked(user)
            }


            }


        }







}