package com.nobug.camerademo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.permissionx.guolindev.PermissionX
import java.io.File


class SystemCameraActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mImageUri: Uri
    private lateinit var mOutImage: File
    private lateinit var btSystemCamera1: Button
    private lateinit var btSystemCamera2: Button
    private lateinit var ivSystemCamera: ImageView
    private val REQUEST_CODE1 = 0x01
    private val REQUEST_CODE2 = 0x02
    private val REQUEST_CODE3 = 0x03

    companion object{
        private const val AUTHORITIES = "com.nobug.camerademo.takephoto.fileprovider"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_systemcamera)
        btSystemCamera1 = findViewById(R.id.btSystemCamera1)
        btSystemCamera1.setOnClickListener(this)
        btSystemCamera2 = findViewById(R.id.btSystemCamera2)
        btSystemCamera2.setOnClickListener(this)
        ivSystemCamera = findViewById(R.id.ivSystemCamera)
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.btSystemCamera1 -> {
                //启动系统相机（不带路径）
                PermissionX.init(this)
                    .permissions(Manifest.permission.CAMERA)
                    .request { _, _, _ ->
                        val intent = Intent()
                        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
                        startActivityForResult(intent, REQUEST_CODE1)
                    }
            }
            R.id.btSystemCamera2 -> {
                //启动系统相机（带缓存路径）
                PermissionX.init(this)
                    .permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .request { _, _, _ ->
                        if (!checkExternalStorageManager()) {
                            openAllFilesAccessPermission(this@SystemCameraActivity)
                        } else {
                            //获得项目缓存路径
                            var filePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "CameraDemo" + File.separator
                            //如果目录不存在则必须创建目录
                            val cameraFolder = File(filePath)
                            if (!cameraFolder.exists()) {
                                cameraFolder.mkdirs()
                            }
                            //根据时间随机生成图片名
                            val photoName: String = System.currentTimeMillis().toString() + ".jpg"
                            filePath += photoName
                            mOutImage = File(filePath)
                            //如果是7.0以上 那么就把uir包装
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                mImageUri = FileProvider.getUriForFile(applicationContext, AUTHORITIES, mOutImage)
                            } else {
                                //否则就用老系统的默认模式
                                mImageUri = Uri.fromFile(mOutImage)
                            }
                            //启动相机
                            val intent = Intent()
                            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
                            startActivityForResult(intent, REQUEST_CODE2)
                        }
                    }
            }
        }
    }

    private fun cropImage(uri: Uri, mOutImage: File): Intent? {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        intent.putExtra("crop", "true") // 可裁剪
        intent.putExtra("aspectX", 1) // 裁剪的宽比例
        intent.putExtra("aspectY", 1) // 裁剪的高比例
        intent.putExtra("outputX", 300) // 裁剪的宽度
        intent.putExtra("outputY", 300) // 裁剪的高度
        // intent.putExtra("spotlightX", 1.1f);//X轴方向的抽屉式壁纸选择框（个别设备有效）
        // intent.putExtra("spotlightY", 2.1f);//Y轴方向的抽屉式壁纸选择框（个别设备有效）
        intent.putExtra("scale", true) // 是否支持缩放
        // intent.putExtra("circleCrop", "true");// 圆形裁剪区域(设置无效)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mOutImage))
        // 是否返回数据
        intent.putExtra("return-data", true)
        // 裁剪成的图片的输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        //是否关闭人脸识别
        intent.putExtra("noFaceDetection", false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        return intent
    }

    /**
     * 检查是否强制分区
     *
     * Android 11 （API 30） 之后，执行了强制分区，强制分区之后，使用 MediaStore 只能访问部分文件（图片、音频、视频），
     * 不能访问其他文件，这时需要添加权限 android.permission.MANAGE_EXTERNAL_STORAGE，
     * 但是该权限并不能默认打开，所以需要跳转到设置页面去打开
     */
    fun checkExternalStorageManager(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) { // 是否有访问所有文件的权限
                return true
            }
        } else {
            return true
        }
        return false
    }

    /**
     * 打开所有文件管理权限
     */
    fun openAllFilesAccessPermission(mContext: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext?.let {
            intent.data = Uri.parse("package:${mContext.packageName}")
            mContext.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === REQUEST_CODE1 && resultCode === RESULT_OK) {
            if (data != null && data.hasExtra("data")) {
                val bitmap: Bitmap? = data.getParcelableExtra("data")
                bitmap.let {
                    ivSystemCamera.setImageBitmap(it)
                }
            }
        } else if(requestCode == REQUEST_CODE2 && resultCode === RESULT_OK){
            if(mImageUri != null){
                //拿到图片资源后开始截图
                startActivityForResult(cropImage(mImageUri, mOutImage), REQUEST_CODE3);
            }
        } else if (requestCode == REQUEST_CODE3 && resultCode === RESULT_OK) {
            if (data != null && data.hasExtra("data")) {
                val bitmap: Bitmap? = data.getParcelableExtra("data")
                bitmap.let {
                    ivSystemCamera.setImageBitmap(it)
                }
            }
        }
    }
}