package com.zh.aidlsample;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zh.aidlsample.service.EncryptDecryptService;
import com.zh.aidlsample.service.TimerService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView vCurrentTime;

    private final ThreadLocal<SimpleDateFormat> mDateFormatThreadLocal = new ThreadLocal<>();

    private IEncryptDecrypt mEncryptDecrypt;
    private ITimer mTimer;

    private final ServiceConnection mEncryptDecryptServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mEncryptDecrypt = IEncryptDecrypt.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mEncryptDecrypt = null;
        }
    };

    private final ServiceConnection mTimberServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTimer = ITimer.Stub.asInterface(service);
            try {
                mTimer.registerCallback(mTimerCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimer = null;
        }
    };

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
        vCurrentTime = findViewById(R.id.current_time);
        Button md5Encrypt = findViewById(R.id.md5_encrypt);
        Button base64Encrypt = findViewById(R.id.base64_encrypt);
        md5Encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEncryptDecrypt != null) {
                    try {
                        String str = "hezihao";
                        String md5Str = mEncryptDecrypt.md5Encrypt(str);
                        Toast.makeText(getApplicationContext(), "md5后字符串：" + md5Str, Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        base64Encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str = "hezihao";
                    String strBase64 = mEncryptDecrypt.base64Encode(str);
                    String base64Decode = mEncryptDecrypt.base64Decode(strBase64);
                    Toast.makeText(getApplicationContext(), "base64编码 =>" + strBase64 + "，base64解码 => " + base64Decode,
                            Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        //加密、解密
        bindService(new Intent(getApplicationContext(), EncryptDecryptService.class), mEncryptDecryptServiceConnection, Context.BIND_AUTO_CREATE);
        //定时器
        bindService(new Intent(getApplicationContext(), TimerService.class), mTimberServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mTimer != null && mTimer.asBinder().isBinderAlive()) {
                mTimer.unRegisterCallback(mTimerCallback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (mEncryptDecryptServiceConnection != null) {
            unbindService(mEncryptDecryptServiceConnection);
        }
        if (mTimberServiceConnection != null) {
            unbindService(mTimberServiceConnection);
        }
    }
}