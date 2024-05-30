package com.example.chatlication.models

import java.io.Serializable

data class UserModel(
    val name:String?=null,
    val image:String?=null,
    val email:String?=null,
    val token:String?=null,
    val id:String?=null
    ):Serializable