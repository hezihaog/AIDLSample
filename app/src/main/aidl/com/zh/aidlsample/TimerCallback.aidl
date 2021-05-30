package com.zh.aidlsample;

//倒计时回调
interface TimerCallback {
    //定时回调
    void onCountDown(long time);
}