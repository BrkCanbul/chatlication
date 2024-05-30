package com.example.chatlication.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatlication.databinding.ItemContainerUserBinding
import com.example.chatlication.listeners.UserListener
import com.example.chatlication.models.UserModel
import com.google.firebase.firestore.auth.User

class UserAdapter(users:List<UserModel>,userlstn:UserListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var users:List<UserModel>
    companion object{
        public fun getUserImage(encodedImage:String):Bitmap{
            val bytes = Base64.decode(encodedImage,Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
        }
        lateinit var userListener:UserListener
    }
    init {
        userListener= userlstn
        this.users = users
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding:ItemContainerUserBinding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserViewHolder(itemContainerUserBinding)

    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users.get(position))
    }

    class UserViewHolder(itemContainerUserBinding: ItemContainerUserBinding) : RecyclerView.ViewHolder(itemContainerUserBinding.root) {
        val binding: ItemContainerUserBinding = itemContainerUserBinding

        fun setUserData(user:UserModel){
            binding.textName.text = user.name
            binding.textEmail.text =user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image.toString()))

            binding.root.setOnClickListener{ userListener.onUserClicked(user)}

        }
    }
}