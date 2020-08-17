// IOnSocketReceivedListener.aidl
package com.neusoft.qiangzi.socketservicedemo;

// Declare any non-default types here with import statements

interface IOnSocketReceivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);
    void onReceived(String data);
}
