package com.example.image.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.image.R
import kotlinx.coroutines.*
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
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

    private var _imageUrl:String = ""

    //Setter
    fun setImageUrl(url:String){
        _imageUrl = url
    }

    //Getter
    fun getImageUrl():String{
        return _imageUrl
    }

    private fun makeStatusNotification(message: String, context: Context) {

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


    suspend fun downloadImage1(context: Context, url: String, fileName: String) {
            try {
                val url = URL(url)
                val inputStream = url.openStream()
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val newFileName = "$fileName-$timeStamp.jpg"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, newFileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
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

    suspend fun downloadImage2(context: Context,imageUrl: String, fileName: String) {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val inputStream = url.openStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val newFileName = "$fileName-$timeStamp.jpg"
                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val file = File(storageDir, newFileName)
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "uri=$uri")
                }
                makeStatusNotification("Download completed",context)

            } catch (e: MalformedURLException) {
                makeStatusNotification("Invalid Url",context)
            } catch (e: Exception) {
                makeStatusNotification("Couldn't download",context)
            }
        }
    }

}












