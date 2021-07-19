// ISocketBinder.aidl
package com.neusoft.qiangzi.socketservicedemo;
import com.neusoft.qiangzi.socketservicedemo.ISocketListener;

// Declare any non-default types here with import statements

interface ISocketBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    const String SERVICE_NAME = "com.neusoft.qiangzi.socketservicedemo.SocketService";

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

    void registerListener(ISocketListener listener);
    void unregisterListener(ISocketListener listener);
}
