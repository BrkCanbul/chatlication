package com.example.chatlication.listeners

import com.example.chatlication.models.UserModel

interface  ConversionListener{
    fun onConversionClicked(user:UserModel)
}