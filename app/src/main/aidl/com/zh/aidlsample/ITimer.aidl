package com.zh.aidlsample;
import com.zh.aidlsample.TimerCallback;

//定时器AIDL
interface ITimer {
    //注册回调
    void registerCallback(TimerCallback callback);

    //注销回调
    void unRegisterCallback(TimerCallback callback);
}