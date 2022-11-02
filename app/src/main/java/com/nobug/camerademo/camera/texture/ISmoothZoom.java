package com.nobug.camerademo.camera.texture;

public interface ISmoothZoom {

    // 平滑缩放成功
    void success();

    // 平滑缩放失败
    void failed();

    // 不支持平滑缩放
    void notSupportZoom();

}
