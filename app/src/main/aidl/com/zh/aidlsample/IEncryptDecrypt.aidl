package com.zh.aidlsample;

//加密、解密AIDL接口
interface IEncryptDecrypt {
    //MD5加密
    String md5Encrypt(String str);

    //base64编码
    String base64Encode(String str);

    //base64解码
    String base64Decode(String base64Str);
}