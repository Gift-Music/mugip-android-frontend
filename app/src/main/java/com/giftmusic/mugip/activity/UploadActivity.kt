package com.giftmusic.mugip.activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.giftmusic.mugip.BaseActivity
import com.giftmusic.mugip.BuildConfig
import com.giftmusic.mugip.R
import com.giftmusic.mugip.models.User
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlin.coroutines.CoroutineContext

class UploadActivity : BaseActivity(), CoroutineScope {
    private lateinit var job : Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        setContentView(R.layout.activity_upload)
        val addTagButton = findViewById<TextView>(R.id.add_tag_button)
        val addTagScrollView = findViewById<ScrollView>(R.id.tag_scroll_view)
        val backButton = findViewById<ImageView>(R.id.back_to_profile)
        val alarmButton = findViewById<ImageView>(R.id.notification_button_profile)
        addTagButton.setOnClickListener {
            when(addTagScrollView.visibility){
                View.VISIBLE -> addTagScrollView.visibility = View.GONE
                View.GONE -> addTagScrollView.visibility = View.VISIBLE
                else -> {}
            }
        }
        backButton.setOnClickListener { finish() }
        alarmButton.setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java)
            startActivity(intent)
        }
        
        val selectCategoryButton = arrayListOf<Button>(
            findViewById(R.id.select_category_exercise),
            findViewById(R.id.select_category_work),
            findViewById(R.id.select_category_reading),
            findViewById(R.id.select_category_drive),
            findViewById(R.id.select_category_trip),
            findViewById(R.id.select_category_programming),
            findViewById(R.id.select_category_shower)
        )
        selectCategoryButton.map {
            button -> button.setOnClickListener {
                addTagButton.text = button.text
                addTagButton.tag = button.id
            }
        }

        val postUploadButton = findViewById<Button>(R.id.post_upload_confirm)
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        postUploadButton.setOnClickListener {
            launch {
                val url = URL(BuildConfig.server_url + "/post/")
                val conn = url.openConnection() as HttpURLConnection
                var errorMessage : String
                try {
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json; utf-8")
                    conn.setRequestProperty("Accept", "application/json")
                    conn.setRequestProperty("Authorization", "Basic ${prefManager.getString("access_token", "")}")
                    conn.doInput = true
                    conn.doOutput = true
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000

                    val requestJson = HashMap<String, String>()
                    requestJson["author_id"] = intent.getBundleExtra("user_id").toString()

                    conn.outputStream.use { os ->
                        val input: ByteArray =
                            Gson().toJson(requestJson).toByteArray(Charsets.UTF_8)
                        os.write(input, 0, input.size)
                        os.flush()
                    }

                    when(conn.responseCode){
                        200 -> {
                            val inputStream = conn.inputStream
                            if(inputStream != null){
                                val returnBody = conn.inputStream.bufferedReader().use(
                                    BufferedReader::readText)
                                val responseJson = JSONObject(returnBody.trim())
                                Log.d("response json", responseJson.toString())

                            }
                        }
                        401 -> moveToLoginActivity()
                        else -> errorMessage = conn.responseCode.toString()
                    }
                }
                catch (e : SocketTimeoutException){
                    errorMessage = "연결 시간 초과 오류"
                }
                catch (e : Exception){
                    Log.e("fetch user information error", e.toString())
                    Log.e("fetch user information error", e.javaClass.kotlin.toString())
                }
                finally {
                    conn.disconnect()
                }
                withContext(Dispatchers.Main){
                    progressOFF()
                }
            }
        }
    }
}