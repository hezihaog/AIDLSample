package com.zh.aidlsample.binder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author wally
 * @date 2021/05/30
 * Binder连接池Service
 */
public class BinderPoolService extends Service {
    private final IBinder mBinder = new BinderPool.BinderPoolImpl();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}