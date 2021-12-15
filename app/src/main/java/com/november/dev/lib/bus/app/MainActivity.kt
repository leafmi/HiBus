package com.november.dev.lib.bus.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.november.dev.lib.bus.HiBus

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        HiBus.with("data", String::class.java).observe(this) {
            Log.d("HiBus", it)
        }

        HiBus.with("data", String::class.java).postValue("Hi Bus")
    }
}