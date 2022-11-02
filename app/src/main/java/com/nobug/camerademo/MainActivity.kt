package com.nobug.camerademo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.nobug.camerademo.camera.surface.SurfaceCameraActivity
import com.nobug.camerademo.camera.texture.TextureCameraActivity
import com.nobug.camerademo.camera2.texture.TextureCamera2Activity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btMainSystemCamera: Button
    private lateinit var btTextureCamera: Button
    private lateinit var btSurfaceCamera: Button
    private lateinit var btTextureCamera2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btMainSystemCamera = findViewById(R.id.btMainSystemCamera)
        btMainSystemCamera.setOnClickListener(this)
        btTextureCamera = findViewById(R.id.btTextureCamera)
        btTextureCamera.setOnClickListener(this)
        btSurfaceCamera = findViewById(R.id.btSurfaceCamera)
        btSurfaceCamera.setOnClickListener(this)
        btTextureCamera2 = findViewById(R.id.btTextureCamera2)
        btTextureCamera2.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btMainSystemCamera -> {
                val intent = Intent(this@MainActivity, SystemCameraActivity::class.java)
                startActivity(intent)
            }
            R.id.btTextureCamera -> {
                val intent = Intent(this@MainActivity, TextureCameraActivity::class.java)
                startActivity(intent)
            }
            R.id.btSurfaceCamera -> {
                val intent = Intent(this@MainActivity, SurfaceCameraActivity::class.java)
                startActivity(intent)
            }
            R.id.btTextureCamera2 -> {
                val intent = Intent(this@MainActivity, TextureCamera2Activity::class.java)
                startActivity(intent)
            }
        }
    }

}