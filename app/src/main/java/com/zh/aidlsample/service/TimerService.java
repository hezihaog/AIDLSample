package com.zh.aidlsample.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zh.aidlsample.ITimer;
import com.zh.aidlsample.TimerCallback;

/**
 * @author wally
 * @date 2021/05/30
 */
public class TimerService extends Service {
    private static final int WHAT_COUNT_DOWN = 1;

    /**
     * 回调列表
     */
    private final RemoteCallbackList<TimerCallback> mCallbacks = new RemoteCallbackList<>();
    /**
     * 一个有Looper循环的子线程
     */
    private HandlerThread mTimberHandlerThread;
    /**
     * 定时器Handler
     */
    private Handler mTimerHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        //开启具有Looper循环的线程
        mTimberHandlerThread = new HandlerThread("TimberHandlerThread");
        mTimberHandlerThread.start();
        //创建绑定了线程中的Looper的Handler，用它来发消息
        mTimerHandler = new Handler(mTimberHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == WHAT_COUNT_DOWN) {
                    try {
                        int count = mCallbacks.beginBroadcast();
                        for (int i = 0; i < count; i++) {
                            TimerCallback callback = mCallbacks.getBroadcastItem(i);
                            callback.onCountDown(System.currentTimeMillis());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mCallbacks.finishBroadcast();
                    //发送延时消息，1秒后再执行
                    sendMessageDelayed(Message.obtain(mTimerHandler,
                            WHAT_COUNT_DOWN,
                            null), 1000);
                }
            }
        };
        //开始定时
        mTimerHandler.sendMessage(Message.obtain(mTimerHandler,
                WHAT_COUNT_DOWN,
                System.currentTimeMillis()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimberHandlerThread.quit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    private class ServiceBinder extends ITimer.Stub {
        @Override
        public void registerCallback(TimerCallback callback) throws RemoteException {
            if (callback != null) {
                mCallbacks.register(callback);
            }
        }

        @Override
        public void unRegisterCallback(TimerCallback callback) throws RemoteException {
            if (callback != null) {
                mCallbacks.unregister(callback);
            }
        }
    }
}