package com.nobug.camerademo.camera.texture;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import java.io.*;
import java.util.List;

public class CameraPreview extends TextureView implements TextureView.SurfaceTextureListener {

    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
    }

    /**
     * 获取相机对象
     * @return
     */
    public Camera getCamera(){
        return mCamera;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // TextureView初始化的时候执行
        Log.d("yunchong", "===onSurfaceTextureAvailable===");
        try {
            // 获取Camera对象
            mCamera = Camera.open();
            // 拍照声音是否使能
            mCamera.enableShutterSound(true);
            //设置预览方向，预览方向默认是横屏的，所以这里要调整一下预览方向
            mCamera.setDisplayOrientation(90);
            Camera.Parameters parameters = mCamera.getParameters();
            // 设置聚焦模式
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //获取最适合的分辨率
            Camera.Size previewSize = getCameraSize(parameters.getSupportedPreviewSizes(), width, height);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            //获取最适合的分辨率
            Camera.Size pictureSize = getCameraSize(parameters.getSupportedPictureSizes(), width, height);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
            //设置拍照之后，本地图片格式
            parameters.setPictureFormat(ImageFormat.JPEG);
            //拍照后的图片文件旋转90度
            parameters.setRotation(90);
            mCamera.setParameters(parameters);
            //将相机的预览效果显示出来
            mCamera.setPreviewTexture(surface);

            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    // 可以不停的返回一帧画面的图像数据
                    // Log.d("yunchong", "=======onPreviewFrame=========");
                }
            });
//            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    // 可以不停的返回一帧画面的图像数据
                      // onSurfaceTextureDestroyed 一起执行
//                    Log.d("yunchong", "=======onPreviewFrame=========");
//                }
//            });

            // 平滑缩放监听
            mCamera.setZoomChangeListener(new Camera.OnZoomChangeListener() {
                @Override
                public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
                    Log.d("yunchong", "=======onZoomChange zoomValue：" + zoomValue);
                }
            });

            mCamera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int error, Camera camera) {
                    switch (error){
                        case Camera.CAMERA_ERROR_UNKNOWN:
                            Log.d("yunchong", "===相机未知错误===");
                            break;
                        case Camera.CAMERA_ERROR_EVICTED:
                            Log.d("yunchong", "===相机已断开连接===");
                            break;
                        case Camera.CAMERA_ERROR_SERVER_DIED:
                            Log.d("yunchong", "===媒体服务器死机===");
                            break;
                        default:
                            Log.d("yunchong", "未知异常error："+error);
                            break;
                    }
                }
            });
            //开启预览
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最合适的Size
     * @param sizeList
     * @param width
     * @param height
     * @return
     */
    private Camera.Size getCameraSize(List<Camera.Size> sizeList, int width, int height){
        Camera.Size tempSize = null;
        float aspectRatio = height * 1.0f / width;//求出预期横宽比
        float offset = aspectRatio;//预期横宽比和实际横宽比误差
        for(Camera.Size size : sizeList){
            if(size.width < height || size.height < width){
                continue;
            }
            //误差最小值
            if(Math.abs(aspectRatio - size.width * 1.0f / size.height) < offset){
                offset = Math.abs(aspectRatio - size.width * 1.0f / size.height);
                tempSize = size;
            }
        }
        if(tempSize == null){
            tempSize = sizeList.get(0);
        }
        return tempSize;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // 当TextureView布局发生改变时执行
        Log.d("yunchong", "===onSurfaceTextureSizeChanged===");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // 当Activity切换到后台或者销毁时执行这个方法
        Log.d("yunchong", "===onSurfaceTextureDestroyed===");
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // 启动预览时执行
        //画布，一帧数据
        // Log.d("yunchong", "===onSurfaceTextureUpdated===");
    }

    /**
     * 拍照
     *
     * @param iTakePicture
     */
    public void takePicture(ITakePicture iTakePicture) {
        if (mCamera == null) {
            iTakePicture.failed();
            return;
        }
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.d("yunchong", "=== takePicture onShutter ===");
            }
        }, null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                iTakePicture.success(data);
                //停止预览
                mCamera.stopPreview();
            }
        });
    }

    /**
     * 改变平滑缩放
     *
     * @param zoomValue
     */
    public void changeZoom(int zoomValue, ISmoothZoom zoom) {
        Camera.Parameters parameters = mCamera.getParameters();
        if(zoomValue < 0 || zoomValue > parameters.getMaxZoom()) {
            zoom.failed();
            return;
        }
        if (parameters.isSmoothZoomSupported()) {
            mCamera.startSmoothZoom(zoomValue);
            zoom.success();
        } else {
            zoom.notSupportZoom();
        }
    }
}
