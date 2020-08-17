package com.neusoft.qiangzi.socketservicedemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.neusoft.qiangzi.socketservicedemo.SocketHelper.TCPHelper;
import com.neusoft.qiangzi.socketservicedemo.SocketHelper.TCPServerHelper;
import com.neusoft.qiangzi.socketservicedemo.SocketHelper.UDPHelper;

public class SocketService extends Service {
    private static final String TAG = "SocketService";
    private int localPort = 6000;
    private int remotePort = 6000;
    private String remoteIP = "127.0.0.1";
    private UDPHelper udpHelper = null;
    private TCPHelper tcpHelper = null;
    private TCPServerHelper tcpServerHelper = null;
    private RemoteCallbackList<IOnSocketReceivedListener> mListenerList = new RemoteCallbackList();

    public SocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: is called.");

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: is called.");
        return new ISocketBinder.Stub() {
            @Override
            public void setUDPEnabled(boolean enabled) throws RemoteException {
                if (enabled) {
                    if(udpHelper==null) {
                        udpHelper = new UDPHelper(localPort);
                        udpHelper.setRemoteIP(remoteIP);
                        udpHelper.setRemotePort(remotePort);
                        udpHelper.setOnUDPReceiveListener(new UDPHelper.OnUDPReceiveListener() {
                            @Override
                            public void onReceived(String data) {
                                Log.d(TAG, "onReceived: data=" + data);
                                broadcastReceivedData(data);
                            }
                        });
                        udpHelper.openSocket();
                        udpHelper.startReceiveData();
                        Log.d(TAG, "setSocketType: UDP init OK.");
                    }else {
                        Log.d(TAG, "setSocketType: UDP is already running...");
                    }
                }else {
                    udpHelper.stopReceiveData();
                    udpHelper.closeSocket();
                    udpHelper = null;
                }
            }

            @Override
            public void setTCPEnabled(boolean enabled) throws RemoteException {
                if (enabled) {
                    if(tcpHelper==null) {
                        tcpHelper = new TCPHelper(remoteIP, remotePort);
                        tcpHelper.setOnReceiveListener(new TCPHelper.OnReceivedListener() {
                            @Override
                            public void onReceived(TCPHelper tcpHelper, String data) {
                                Log.d(TAG, "onReceived: data=" + data);
                                broadcastReceivedData(data);
                            }
                        });
                        tcpHelper.setOnTCPEventListener(new TCPHelper.OnTCPEventListener() {
                            @Override
                            public void onTcpEvent(TCPHelper.TCP_EVENT e) {
                                switch (e){
                                    case TCP_OPEN_SUCCESS:
                                        tcpHelper.startReceiveData();
                                        break;
                                    case TCP_OPEN_FAILED:
                                        break;
                                    case TCP_OPEN_TIMEOUT:
                                        break;
                                    case TCP_SEND_ERROR:
                                        break;
                                    case TCP_RECV_ERROR:
                                        break;
                                    case TCP_BREAK_OFF:
                                        break;
                                }
                            }
                        });
                        tcpHelper.openSocket();

                        Log.d(TAG, "setSocketType: TCP init OK.");
                    }else {
                        Log.d(TAG, "setSocketType: UDP is already running...");
                    }
                }else {
                    tcpHelper.stopReceiveData();
                    tcpHelper.closeSocket();
                    tcpHelper = null;
                }
            }

            @Override
            public void setTCPServerEnabled(boolean enabled) throws RemoteException {
                if(enabled){
                    if(tcpServerHelper==null){
                        tcpServerHelper = new TCPServerHelper(localPort);
                        tcpServerHelper.setOnAcceptListener(new TCPServerHelper.OnAcceptListener() {
                            @Override
                            public void onAccepted(TCPHelper tcpClient) {
                                tcpClient.setOnReceiveListener(new TCPHelper.OnReceivedListener() {
                                    @Override
                                    public void onReceived(TCPHelper tcpHelper, String data) {
                                        Log.d(TAG, "onReceived: data=" + data);
                                        broadcastReceivedData(data);
                                    }
                                });
                            }
                        });
                        tcpServerHelper.listenStart();
                        Log.d(TAG, "setTCPServerEnabled: TCP Server is init OK.");
                    }else {
                        Log.d(TAG, "setTCPServerEnabled: TCP Server is already running...");
                    }
                }else {
                    tcpServerHelper.dropAllClient();
                    tcpServerHelper.listenStop();
                    tcpServerHelper = null;
                }
            }

            @Override
            public boolean isUDPEnabled() throws RemoteException {
                return udpHelper!=null;
            }

            @Override
            public boolean isTCPEnabled() throws RemoteException {
                return tcpHelper!=null;
            }

            @Override
            public boolean isTCPServerEnabled() throws RemoteException {
                return tcpServerHelper!=null;
            }

            @Override
            public void setLocalPort(int Port) throws RemoteException {
                SocketService.this.localPort = Port;
                if(udpHelper!=null) {
                    udpHelper.setLocalPort(Port);
                    udpHelper.restartReceiveData();
                }
                if(tcpServerHelper!=null){
                    tcpServerHelper.setLocalPort(Port);
                }
            }

            @Override
            public int getLocalPortPort() throws RemoteException {
                return localPort;
            }

            @Override
            public void setRemoteIP(String ip) throws RemoteException {
                SocketService.this.remoteIP = ip;
                if(udpHelper!=null){
                    udpHelper.setRemoteIP(ip);
                }
                if(tcpHelper!=null){
                    tcpHelper.setRemoteIP(ip);
                }
            }

            @Override
            public String getRemoteIP() throws RemoteException {
                return remoteIP;
            }

            @Override
            public void setRemotePort(int port) throws RemoteException {
                SocketService.this.remotePort = port;
                if(udpHelper!=null){
                    udpHelper.setRemotePort(port);
                }
                if(tcpHelper!=null){
                    tcpHelper.setRemotePort(port);
                }
            }

            @Override
            public int getRemotePort() throws RemoteException {
                return remotePort;
            }

            @Override
            public void sendText(String text) throws RemoteException {
//                Log.d(TAG, "sendText: text="+text);
                if(udpHelper!=null){
                    udpHelper.send(text);
                }
                if(tcpHelper!=null){
                    tcpHelper.send(text);
                }
                if(tcpServerHelper!=null){
                    tcpServerHelper.sendToAll(text);
                }
            }

            @Override
            public void connect(String remoteIp, int remotePort) throws RemoteException {
                if(tcpHelper!=null) {
                    tcpHelper.openSocket();
                    tcpHelper.startReceiveData();
                }
            }

            @Override
            public void disconnect(String remoteIp, int remotePort) throws RemoteException {
                if(tcpHelper!=null && tcpHelper.getRemoteIP().equals(remoteIp)
                &&tcpHelper.getRemotePort()==remotePort)
                {
                    tcpHelper.stopReceiveData();
                    tcpHelper.closeSocket();
                }
                else if(tcpServerHelper!=null){
                    tcpServerHelper.dropClient(remoteIp,remotePort);
                }
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

    private void broadcastReceivedData(String data) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(udpHelper != null && udpHelper.isOpen()){
            udpHelper.closeSocket();
            udpHelper = null;
        }
    }
}
