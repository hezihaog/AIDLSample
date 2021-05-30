package com.zh.aidlsample.binder;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.zh.aidlsample.IBinderPool;
import com.zh.aidlsample.impl.EncryptDecryptImpl;
import com.zh.aidlsample.impl.TimerImpl;

import java.util.concurrent.CountDownLatch;

/**
 * @author wally
 * @date 2021/05/30
 * Binder连接池
 */
public class BinderPool {
    @SuppressLint("StaticFieldLeak")
    private static volatile BinderPool sInstance;
    private final Context mContext;
    private IBinderPool mBinderPool;
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    private BinderPool(Context context) {
        mContext = context;
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private final ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBinderPool = IBinderPool.Stub.asInterface(iBinder);
            //设置Binder死亡代理
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /**
     * 连接服务
     */
    private synchronized void connectBinderPoolService() {
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(mContext, BinderPoolService.class);
        mContext.bindService(intent, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
        try {
            mConnectBinderPoolCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 死亡监听
     */
    private final IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            mBinderPool.asBinder().unlinkToDeath(this, 0);
            mBinderPool = null;
            //重连
            connectBinderPoolService();
        }
    };

    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return binder;
    }

    /**
     * 加解密
     */
    public static final int BINDER_ENCRYPT_DECRYPT = 0;
    /**
     * 定时器
     */
    public static final int BINDER_TIMER = 1;

    public static class BinderPoolImpl extends IBinderPool.Stub {
        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            switch (binderCode) {
                case BINDER_TIMER:
                    return new EncryptDecryptImpl();
                case BINDER_ENCRYPT_DECRYPT:
                    return new TimerImpl();
                default:
                    return null;
            }
        }
    }
}