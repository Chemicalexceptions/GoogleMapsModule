package com.example.googlemapsmodule

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.util.*
import java.util.jar.Manifest

class MyBackgroudLoactionService : Service() {


    var TAG = MyBackgroudLoactionService::class.java.simpleName

    lateinit var mFusedLocationProviderClient :FusedLocationProviderClient
    lateinit var mlocationCallback : LocationCallback


    override fun onCreate() {
        super.onCreate()

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
    }


    override fun onBind(intent: Intent): IBinder? {

        return null



    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e(TAG,"on start command called")

        startForeground(1001,generateNotification())

        getLocationUpdates()

        return START_STICKY
    }

    private fun generateNotification(): Notification? {

       var notificationCompat = NotificationCompat.Builder(applicationContext,App.CHANNEL_ID)
           .setContentTitle("Background Location Notification")
           .setSmallIcon(R.mipmap.ic_launcher)

        return notificationCompat.build()

    }

    private fun getLocationUpdates() {

        var locationRequest = LocationRequest.create()
        locationRequest.fastestInterval = 1000
        locationRequest.interval = 3000
        locationRequest.maxWaitTime = 5*1000
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){


            stopForeground(true)
            stopSelf()
            return
        }

      mlocationCallback =  object : LocationCallback() {
          override fun onLocationResult(result: LocationResult?) {


              if (result == null) {

                  return
              }

              var location = result.lastLocation

              Log.e(TAG, "${location.latitude}__ ${location.longitude}")
              Log.e(TAG, "Thread name ${Thread.currentThread().name}")


          }
      }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,mlocationCallback
            , Looper.myLooper())

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e(TAG,"onDestroy : Called")
        stopForeground(true)
        mFusedLocationProviderClient.removeLocationUpdates(mlocationCallback)

    }


}
