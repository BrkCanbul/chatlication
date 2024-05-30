package com.example.chatlication.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Firebase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MesssagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM","TOKEN: "+token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FCM","Message"+message.getNotification().getBody());

    }
}
