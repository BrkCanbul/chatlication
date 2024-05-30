package com.example.chatlication.models
import java.io.Serializable
import java.util.Date

data class ChatMessage(
    var senderId:String?=null,
    var receiverId:String?=null,
    var dateTime:String?=null,
    var message:String?=null,
    var dateObj:Date?=null,
    var conversionId:String?=null,
    var conversionName:String?=null,
    var conversionImage:String?=null
):Serializable