package com.nobug.camerademo.camera2.texture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Camera2Preview extends AutoFitTextureView implements TextureView.SurfaceTextureListener {

    private CameraDevice cameraDevice;
    private Context context;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private Surface imageReaderSurface;
    private ITakePicture iTakePicture;
    private CameraManager cameraManager;
    private CaptureRequest.Builder requestBuilderImageReader;
    private CaptureRequest captureRequest;

    public Camera2Preview(Context context) {
        super(context);
        this.context = context;
    }

    public Camera2Preview(Context context, ITakePicture iTakePicture) {
        super(context);
        this.context = context;
        this.iTakePicture = iTakePicture;
    }

    /**
     * 获取相机对象
     * @return
     */
    public CameraDevice getCamera(){
        return cameraDevice;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // TextureView初始化的时候执行
        try {
            // 获取CameraManager
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds == null || cameraIds.length <= 0) {
                return;
            }
            String cameraId = cameraIds[0];
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (null != facing) {
                    // CameraCharacteristics.LENS_FACING_BACK：后置摄像头
                    // CameraCharacteristics.LENS_FACING_FRONT：前置摄像头
                    // CameraCharacteristics.LENS_FACING_EXTERNAL：外置摄像头
                    if(facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        // 如果有前置摄像头，则取前置摄像头
                        cameraId = id;
                        break;
                    }
                }
            }
            //获取指定相机的输出尺寸列表
            List<Size> outputSizes = getCameraOutputSizes(cameraId, SurfaceTexture.class);
            // 获取最佳尺寸
            Size previewSize = getCameraSize(outputSizes, width, height);
            // 重置预览界面大小
            setAspectRation(previewSize.getWidth(), previewSize.getHeight());
            Log.d("yunchong", "=== 获取最佳尺寸 ===" + previewSize.getWidth()+ "==" + previewSize.getHeight());
            // 初始化预览尺寸
            surface.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            // 初始化ImageReader
            imageReader = ImageReader.newInstance(previewSize.getWidth() ,previewSize.getHeight(), ImageFormat.JPEG,2);
            // 设置ImageReader收到图片后的回调函数
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // 接收图片：从ImageReader中读取最近的一张，转成Bitmap
                    Log.d("yunchong", "=== 接收到一张图片 ===");
                    Image image= reader.acquireLatestImage();
                    ByteBuffer buffer= image.getPlanes()[0].getBuffer();
                    int length= buffer.remaining();
                    byte[] bytes= new byte[length];
                    buffer.get(bytes);
                    image.close();
                    if (iTakePicture != null) {
                        iTakePicture.success(bytes);
                    }
                }
            },null);

            // 打开相机的回调
            CameraDevice.StateCallback cam_stateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    Log.d("yunchong", "=== onOpened ===");
                    cameraDevice = camera;
                    Surface textureSurface = new Surface(surface);
                    imageReaderSurface = imageReader.getSurface();
                    try {
                        // 创建CaptureRequest
                        CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        requestBuilder.addTarget(textureSurface);
                        captureRequest = requestBuilder.build();

                        CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                cameraCaptureSession = session;
                                try {
                                    session.setRepeatingRequest(captureRequest,null,null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                            }
                        };
                        // 创建Capture会话
                        cameraDevice.createCaptureSession(Arrays.asList(textureSurface, imageReaderSurface), stateCallback,null);
                        initRequestBuilderImageReader();
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onDisconnected(CameraDevice camera) {
                    Log.d("yunchong", "=== onDisconnected ===");
                }
                @Override
                public void onError(CameraDevice camera, int error) {
                    Log.d("yunchong", "=== onError ===");
                }
            };
            // 打开相机，handler：回调执行的Handler对象，传入null则使用当前的主线程Handler
            cameraManager.openCamera(cameraId, cam_stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initRequestBuilderImageReader() throws CameraAccessException {
        requestBuilderImageReader = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        // 旋转90度
        requestBuilderImageReader.set(CaptureRequest.JPEG_ORIENTATION, 270);
        // AE模式
        requestBuilderImageReader.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        // 关闭闪光灯
        requestBuilderImageReader.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
        // 配置request的参数的目标对象
        requestBuilderImageReader.addTarget(imageReaderSurface);
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
        // 先把相机的session关掉
        if(cameraCaptureSession != null){
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
        // 再关闭相机
        if(null != cameraDevice){
            cameraDevice.close();
            cameraDevice = null;
        }
        // 最后关闭ImageReader
        if(null != imageReader){
            imageReader.close();
            imageReader = null;
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
     */
    public void takePicture() {
        Log.d("yunchong", "=== takePicture ===");
        try {
            // 触发拍照
            cameraCaptureSession.capture(requestBuilderImageReader.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    if (iTakePicture != null) {
                        iTakePicture.failed();
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据输出类获取指定相机的输出尺寸列表
     * @param cameraId 相机id
     * @param clz 输出类
     * @return
     */
    public List<Size> getCameraOutputSizes(String cameraId, Class clz){
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            return Arrays.asList(configs.getOutputSizes(clz));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 根据输出格式获取指定相机的输出尺寸列表
     * @param cameraId 相机id
     * @param format 输出格式
     * @return
     */
    public List<Size> getCameraOutputSizes(String cameraId, int format){
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            return Arrays.asList(configs.getOutputSizes(format));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取最合适的Size
     * @param sizeList
     * @param width
     * @param height
     * @return
     */
    private Size getCameraSize(List<Size> sizeList, int width, int height){
        Size tempSize = null;
        float aspectRatio = height * 1.0f / width; // 求出预期横宽比
        float offset = aspectRatio; // 预期横宽比和实际横宽比误差
        for(Size size : sizeList){
            if(size.getWidth() < height || size.getHeight() < width){
                continue;
            }
            //误差最小值
            if(Math.abs(aspectRatio - size.getWidth() * 1.0f / size.getHeight()) < offset){
                offset = Math.abs(aspectRatio - size.getWidth() * 1.0f / size.getHeight());
                tempSize = size;
            }
        }
        if(tempSize == null){
            tempSize = sizeList.get(0);
        }
        return tempSize;
    }

    /**
     * 切换闪光灯
     */
//    public void switchFlash(boolean isFlashing) {
//        if (isFlashing) {
//            requestBuilderImageReader.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//            requestBuilderImageReader.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
//            captureRequest = requestBuilderImageReader.build();
//            try {
//                cameraCaptureSession.setRepeatingRequest(captureRequest, null, null);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        } else {
//            requestBuilderImageReader.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//            requestBuilderImageReader.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
//            captureRequest = requestBuilderImageReader.build();
//            try {
//                cameraCaptureSession.setRepeatingRequest(captureRequest, null, null);
//            } catch (CameraAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
