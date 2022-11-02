package com.nobug.camerademo.camera.surface

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.nobug.camerademo.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SurfaceCameraActivity : AppCompatActivity(), View.OnClickListener {

    private var mPreview: CameraPreview? = null
    private var fl_camera_preview: FrameLayout? = null
    private var takePhone: ImageView? = null
    private var flash_button: ImageView? = null

    //是否开启闪光灯
    private var isFlashing = false

    //图片临时缓存
    private var imageData: ByteArray? = null

    //是否正在聚焦
    private val isFoucing = false

    //自定义聚焦视图
    //private FocusCameraView mFocusCameraView;
    private var save_button: ImageView? = null
    private var cancle_save_button: ImageView? = null
    private var cancle_button: Button? = null
    private var zoom_button: Button? = null
    private var ll_photo_layout: RelativeLayout? = null
    private var ll_confirm_layout: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_texturecamera)
        fl_camera_preview = findViewById(R.id.camera_preview)
        takePhone = findViewById(R.id.take_photo_button)
        takePhone?.setOnClickListener(this)
        flash_button = findViewById(R.id.flash_button)
        flash_button?.setOnClickListener(this)
        ll_photo_layout = findViewById(R.id.ll_photo_layout)
        ll_confirm_layout = findViewById(R.id.ll_confirm_layout)
        save_button = findViewById(R.id.save_button)
        save_button?.setOnClickListener(this)
        cancle_save_button = findViewById(R.id.cancle_save_button)
        cancle_save_button?.setOnClickListener(this)
        cancle_button = findViewById(R.id.cancle_button)
        cancle_button?.setOnClickListener(this)
        zoom_button = findViewById(R.id.zoom_button)
        zoom_button?.setOnClickListener(this)

        // 创建预览界面
        mPreview = CameraPreview(this)
        //设置透明度
        mPreview?.alpha = 1f

        //添加监听
        mPreview?.getHolder()?.addCallback(mPreview);

        //将预览页面添加到FrameLayout中
        fl_camera_preview?.addView(mPreview)
    }

    /**
     * 调用摄像头开始拍照片
     */
    private fun takePhoto() {
        mPreview?.let {
            //调用相机拍照
            it.takePicture(object : ITakePicture {

                @SuppressLint("ObjectAnimatorBinding")
                override fun success(data: ByteArray?) {
                    //视图动画
                    ll_photo_layout?.visibility = View.GONE
//                    val anim1 = ObjectAnimator.ofFloat(ll_confirm_layout, "scaleX", 0f, 1.0f)
//                    val anim2 = ObjectAnimator.ofFloat(ll_confirm_layout, "scaleY", 0f, 1.0f)
//                    val animatorSet = AnimatorSet()
//                    animatorSet.duration = 500
//                    animatorSet.play(anim1).with(anim2)
//                    animatorSet.interpolator = AccelerateDecelerateInterpolator()
//                    animatorSet.start()
                    ll_confirm_layout?.visibility = View.VISIBLE
                    //停止预览
                    imageData = data
                }

                override fun failed() {
                    Log.e("yunchong", "=== 拍照失败 ===")
                }

            })
        }
    }

    /**
     * 保存照片
     * @param imageData
     */
    private fun savePhoto(imageData: ByteArray?) {
        if (imageData != null && imageData.isEmpty()) {
            return
        }
        var fos: FileOutputStream? = null
        val cameraPath = Environment.getExternalStorageDirectory().absolutePath + File.separator + "CameraDemo"
        //相册文件夹
        val cameraFolder = File(cameraPath)
        if (!cameraFolder.exists()) {
            cameraFolder.mkdirs()
        }
        //保存的图片文件
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val imagePath: String =
            cameraFolder.absolutePath + File.separator.toString() + "IMG_" + simpleDateFormat.format(
                Date()
            ).toString() + ".jpg"
        val imageFile = File(imagePath)
        try {
            fos = FileOutputStream(imageFile)
            fos.write(imageData)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 切换闪光灯
     */
    private fun switchFlash() {
        val mCamera = mPreview!!.camera ?: return
        isFlashing = !isFlashing
        flash_button?.setImageResource(if (isFlashing) R.mipmap.flash_open else R.mipmap.flash_close)
        try {
            val parameters = mCamera.parameters
            parameters.flashMode =
                if (isFlashing) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            mCamera.parameters = parameters
        } catch (e: Exception) {
            Toast.makeText(this, "该设备不支持闪光灯", Toast.LENGTH_SHORT)
        }
    }

    /**
     * 快速切换到预览状态
     */
    @SuppressLint("ObjectAnimatorBinding")
    private fun changePreview() {
        val mCamera = mPreview?.camera ?: return
        ll_confirm_layout?.visibility = View.GONE
//        val anim1 = ObjectAnimator.ofFloat(ll_photo_layout, "scaleX", 0f, 1.0f)
//        val anim2 = ObjectAnimator.ofFloat(ll_photo_layout, "scaleY", 0f, 1.0f)
//        val animatorSet = AnimatorSet()
//        animatorSet.duration = 500
//        animatorSet.play(anim1).with(anim2)
//        animatorSet.interpolator = AccelerateDecelerateInterpolator()
//        animatorSet.start()
        ll_photo_layout?.visibility = View.VISIBLE

        //开始预览
        mCamera.startPreview()
        imageData = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.take_photo_button -> takePhoto()         //拍照
            R.id.flash_button -> switchFlash()      //切换闪光灯
            R.id.save_button -> {
                //保存照片
                savePhoto(imageData)
                //切换到预览状态
                changePreview()
            }
            R.id.cancle_save_button -> changePreview()             //切换到预览状态
            R.id.cancle_button -> finish()
            R.id.zoom_button -> { // 平滑缩放
                mPreview?.changeZoom(1, object : ISmoothZoom {

                    override fun success() {
                        Log.d("yunchong", "=== changeZoom success ===")
                    }

                    override fun failed() {
                        Log.d("yunchong", "=== changeZoom failed ===")
                    }

                    override fun notSupportZoom() {
                        Log.d("yunchong", "=== changeZoom notSupportZoom ===")
                    }
                })
            }
        }
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
}