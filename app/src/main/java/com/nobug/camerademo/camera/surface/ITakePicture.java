package com.nobug.camerademo.camera.surface;

public interface ITakePicture {

    // 拍照成功
    void success(byte[] data);

    // 拍照失败
    void failed();

}
