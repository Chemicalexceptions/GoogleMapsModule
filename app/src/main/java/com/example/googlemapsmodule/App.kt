package com.example.googlemapsmodule

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class App : Application(){

    companion object{

        var CHANNEL_ID = "default_channel"

    }


    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            var notificationChannel = NotificationChannel(CHANNEL_ID,"location_channel",NotificationManager.IMPORTANCE_HIGH)
            var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }





    }
}