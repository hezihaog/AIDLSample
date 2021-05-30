package com.zh.aidlsample;
import com.zh.aidlsample.TimerCallback;

//定时器AIDL，每秒获取一次当前时间
interface ITimer {
    //开启定时器
    void startTimer();

    //停止定时器
    void stopTimer();

    //注册回调
    void registerCallback(TimerCallback callback);

    //注销回调
    void unRegisterCallback(TimerCallback callback);
}