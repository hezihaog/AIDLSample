package com.zh.aidlsample.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.zh.aidlsample.IEncryptDecrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wally
 * @date 2021/05/30
 * 加解密Service
 */
public class EncryptDecryptService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    private static class ServiceBinder extends IEncryptDecrypt.Stub {
        @Override
        public String md5Encrypt(String str) throws RemoteException {
            return md5(str);
        }

        @Override
        public String base64Encode(String str) throws RemoteException {
            return Base64.encodeToString(str.getBytes(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
        }

        @Override
        public String base64Decode(String base64Str) throws RemoteException {
            return new String(Base64.decode(base64Str, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE));
        }

        /**
         * md5加密
         */
        public static String md5(String string) {
            if (TextUtils.isEmpty(string)) {
                return "";
            }
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
                byte[] bytes = md5.digest(string.getBytes());
                StringBuilder result = new StringBuilder();
                for (byte b : bytes) {
                    String temp = Integer.toHexString(b & 0xff);
                    if (temp.length() == 1) {
                        temp = "0" + temp;
                    }
                    result.append(temp);
                }
                return result.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}