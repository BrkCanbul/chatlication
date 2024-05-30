package com.example.chatlication.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.contentValuesOf

class PreferenceManager(context: Context) {
    var sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCES,Context.MODE_PRIVATE)
    }
    fun putBoolean(key:String ,value :Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean(key,value)
        editor.apply()

    }
    fun getBoolean(key:String): Boolean {
        return sharedPreferences.getBoolean(key,false)
    }
    fun clear(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    fun putString(key:String,value:String){
        val editor = sharedPreferences.edit()
        editor.putString(key,value)
        editor.apply()

    }
    fun getString(key:String): String? {
        return sharedPreferences.getString(key,null)


    }

}