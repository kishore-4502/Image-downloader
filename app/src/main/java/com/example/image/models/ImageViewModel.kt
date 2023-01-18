package com.example.image.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.image.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

// Name of Notification Channel for verbose notifications of background work
@JvmField val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notification"
@JvmField val NOTIFICATION_TITLE: CharSequence = "Notification"
const val CHANNEL_ID = "ID_NOTIFICATION"
const val NOTIFICATION_ID = 1

class ImageViewModel:ViewModel() {
    private var _imageUrl= MutableLiveData<String>("")
    val imageUrl:LiveData<String> = _imageUrl



    //Setter
    fun setImageUrl(url:String){
        _imageUrl.value = url
    }

    //Getter
    fun getImageUrl():String{
        return _imageUrl.value!!
    }

    fun makeStatusNotification(message: String, context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
            val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description

            // Add the channel
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

            notificationManager?.createNotificationChannel(channel)
        }

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        // Show the notification
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }


    suspend fun downloadImage(context: Context, url: String, fileName: String) {
        try {
            val url = URL(url)
            val inputStream = url.openStream()
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            }

            val uri = withContext(Dispatchers.IO) {
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }

            val outputStream = context.contentResolver.openOutputStream(uri!!)
            inputStream.use { it.copyTo(outputStream!!) }
            outputStream?.close()
            makeStatusNotification("Download completed",context)
        } catch (e: MalformedURLException) {
            makeStatusNotification("Invalid Url",context)
        } catch (e: Exception) {
            makeStatusNotification("Couldn't download",context)
        }
    }

}