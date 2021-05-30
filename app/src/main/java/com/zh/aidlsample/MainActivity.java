package com.zh.aidlsample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zh.aidlsample.binder.BinderPool;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView vCurrentTime;
    private Button vMd5Encrypt;
    private Button vBase64Encrypt;

    private final ThreadLocal<SimpleDateFormat> mDateFormatThreadLocal = new ThreadLocal<>();
    private ITimer mTimer;
    private IEncryptDecrypt mDecrypt;

    private final TimerCallback mTimerCallback = new TimerCallback.Stub() {
        @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
        @Override
        public void onCountDown(long time) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SimpleDateFormat format = mDateFormatThreadLocal.get();
                    if (format == null) {
                        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        mDateFormatThreadLocal.set(format);
                    }
                    String formatStr = format.format(new Date(time));
                    vCurrentTime.setText("当前时间：" + formatStr);
                    Log.d(TAG, "time = " + time + "，格式化：" + formatStr);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        bindView();
        setData();
    }

    private void findView() {
        vCurrentTime = findViewById(R.id.current_time);
        vMd5Encrypt = findViewById(R.id.md5_encrypt);
        vBase64Encrypt = findViewById(R.id.base64_encrypt);
    }

    private void bindView() {
        //md5加密
        vMd5Encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str = "hezihao";
                    String md5Str = mDecrypt.md5Encrypt(str);
                    Toast.makeText(getApplicationContext(), "md5后字符串：" + md5Str, Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        vBase64Encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str = "hezihao";
                    String strBase64 = mDecrypt.base64Encode(str);
                    String base64Decode = mDecrypt.base64Decode(strBase64);
                    Toast.makeText(getApplicationContext(), "base64编码 =>" + strBase64 + "，base64解码 => " + base64Decode,
                            Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setData() {
        //使用BinderPool，必须开子线程，内部的CountDownLatch，会卡住线程，如果在主线程中调用，就会卡死主线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                BinderPool binderPool = BinderPool.getInstance(getApplicationContext());
                mTimer = ITimer.Stub.asInterface(
                        binderPool.queryBinder(BinderPool.BINDER_TIMER)
                );
                mDecrypt = IEncryptDecrypt.Stub.asInterface(
                        binderPool.queryBinder(BinderPool.BINDER_ENCRYPT_DECRYPT)
                );
                try {
                    mTimer.startTimer();
                    mTimer.registerCallback(mTimerCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mTimer != null && mTimer.asBinder().isBinderAlive()) {
                mTimer.stopTimer();
                mTimer.unRegisterCallback(mTimerCallback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}