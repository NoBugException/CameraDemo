package com.nobug.camerademo.camera.texture;

public interface ITakePicture {

    // 拍照成功
    void success(byte[] data);

    // 拍照失败
    void failed();

}
