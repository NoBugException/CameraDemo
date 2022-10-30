package com.nobug.camerademo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btMainSystemCamera: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btMainSystemCamera = findViewById(R.id.btMainSystemCamera)
        btMainSystemCamera.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btMainSystemCamera -> {
                val intent = Intent(this@MainActivity, SystemCameraActivity::class.java)
                startActivity(intent)
            }
        }
    }

}