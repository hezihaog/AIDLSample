package com.zh.aidlsample;

//Binder连接池
interface IBinderPool {
    //查询Binder
    IBinder queryBinder(int binderCode);
}