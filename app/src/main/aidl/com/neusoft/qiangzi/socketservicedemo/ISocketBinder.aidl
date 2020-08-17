// ISocketBinder.aidl
package com.neusoft.qiangzi.socketservicedemo;
import com.neusoft.qiangzi.socketservicedemo.IOnSocketReceivedListener;

// Declare any non-default types here with import statements

interface ISocketBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);
    void setLocalPort(int Port);
    int getLocalPortPort();
    void setRemoteIP(String ip);
    String getRemoteIP();
    void setRemotePort(int port);
    int getRemotePort();

    void registerListener(IOnSocketReceivedListener listener);
    void unregisterListener(IOnSocketReceivedListener listener);
}
