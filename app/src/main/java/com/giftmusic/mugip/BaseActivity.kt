package com.giftmusic.mugip

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.giftmusic.mugip.activity.LoginActivity

open class BaseActivity : AppCompatActivity() {
    fun progressOn(){
        BaseApplication.getInstance().progressOn(this, null)
    }

    fun progressOn(message: String){
        BaseApplication.getInstance().progressOn(this, message)
    }

    fun progressOFF(){
        BaseApplication.getInstance().progressOFF()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(BaseApplication.getInstance().progressDialog != null && BaseApplication.getInstance().progressDialog!!.isShowing){
            BaseApplication.getInstance().progressDialog!!.dismiss()
        }
    }

    fun moveToLoginActivity(){
        val prefManager = this.getSharedPreferences("app", Context.MODE_PRIVATE)
        val editor = prefManager.edit()
        editor.putString("access_token", null).apply()
        editor.putString("refresh_token", null).apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("logout", true)
        startActivity(intent)
        finish()
    }
}