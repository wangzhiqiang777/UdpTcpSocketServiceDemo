package com.neusoft.qiangzi.socketservicedemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.neusoft.qiangzi.socketservicedemo.SocketHelper.UDPHelper;

public class SocketService extends Service {
    private static final String TAG = "SocketService";
    UDPHelper udpHelper = null;
    RemoteCallbackList<IOnSocketReceivedListener> mListenerList = new RemoteCallbackList();

    public SocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: is called.");
        udpHelper = new UDPHelper();
        udpHelper.setOnUDPReceiveListener(new UDPHelper.OnUDPReceiveListener() {
            @Override
            public void onReceived(String data) {
                synchronized (mListenerList) {
                    int n = mListenerList.beginBroadcast();
                    try {
                        for (int i = 0; i < n; i++) {
                            IOnSocketReceivedListener listener = mListenerList.getBroadcastItem(i);
                            if (listener != null) {
                                listener.onReceived(data);
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mListenerList.finishBroadcast();
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: is called.");
        return new ISocketBinder.Stub() {
            @Override
            public void setLocalPort(int Port) throws RemoteException {
                udpHelper.setLocalPort(Port);
            }

            @Override
            public int getLocalPortPort() throws RemoteException {
                return udpHelper.getLocalPortPort();
            }

            @Override
            public void setRemoteIP(String ip) throws RemoteException {
                udpHelper.setRemoteIP(ip);
            }

            @Override
            public String getRemoteIP() throws RemoteException {
                return udpHelper.getRemoteIP();
            }

            @Override
            public void setRemotePort(int port) throws RemoteException {
                udpHelper.setRemotePort(port);
            }

            @Override
            public int getRemotePort() throws RemoteException {
                return udpHelper.getRemotePort();
            }

            @Override
            public void registerListener(IOnSocketReceivedListener listener) throws RemoteException {
                mListenerList.register(listener);
                Log.d(TAG, "registerListener: current size:" + mListenerList.getRegisteredCallbackCount());
            }

            @Override
            public void unregisterListener(IOnSocketReceivedListener listener) throws RemoteException {
                mListenerList.unregister(listener);
                Log.d(TAG, "unregisterListener: current size:" + mListenerList.getRegisteredCallbackCount());
            }

        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(udpHelper != null && udpHelper.isOpen()){
            udpHelper.closeSocket();
            udpHelper = null;
        }
    }
}
