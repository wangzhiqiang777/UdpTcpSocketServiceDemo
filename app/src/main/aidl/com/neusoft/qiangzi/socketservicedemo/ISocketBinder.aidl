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
    void setUDPEnabled(boolean enabled);
    void setTCPEnabled(boolean enabled);
    void setTCPServerEnabled(boolean enabled);
    boolean isUDPEnabled();
    boolean isTCPEnabled();
    boolean isTCPServerEnabled();

    void setLocalPort(int Port);
    int getLocalPortPort();
    void setRemoteIP(String ip);
    String getRemoteIP();
    void setRemotePort(int port);
    int getRemotePort();

    void sendText(String text);
    void connect(String remoteIp, int remotePort);
    void disconnect(String remoteIp, int remotePort);

    void registerListener(IOnSocketReceivedListener listener);
    void unregisterListener(IOnSocketReceivedListener listener);
}
