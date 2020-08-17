package com.neusoft.qiangzi.socketservicedemo.SocketHelper;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class TCPServerHelper {
    private final String TAG = "TCPServerHelper";
    private int localPort = 7000;
    private ServerSocket mServerSocket = null;
    private List<TCPHelper> acceptSocketList = new ArrayList<>();
    private OnAcceptListener onAcceptListener = null;
    private Thread listenThread = null;
    private boolean isListenStart = false;


    public TCPServerHelper() {
    }

    public TCPServerHelper(int localPort) {
        this.localPort = localPort;
    }

    public void setLocalPort(int Port) {
        this.localPort = Port;
    }

    public int getLocalPortPort() {
        return localPort;
    }

    public void listenStart() {
        if(isListenStart)return;
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "listenThread: Begin!");
                try {
                    mServerSocket = new ServerSocket(localPort);
                    mServerSocket.setSoTimeout(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isListenStart = true;
                while (isListenStart) {
                    try {
                        Socket socket = mServerSocket.accept();
                        TCPHelper tcpHelper = new TCPHelper(socket);
                        acceptSocketList.add(tcpHelper);
                        tcpHelper.startReceiveData();
                        if(onAcceptListener!=null) onAcceptListener.onAccepted(tcpHelper);
                        Log.d(TAG, "listenThread: accept new client: ip="
                                +tcpHelper.getRemoteIP()+",port="+tcpHelper.getRemotePort());
                    } catch (SocketTimeoutException e) {
                        Log.d(TAG, "listenThread: is waiting connect...");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "listenThread: End!");
            }
        });
        listenThread.start();
    }

    public void listenStop(){
        if(!isListenStart)return;
        isListenStart = false;
        if(listenThread!=null && listenThread.isAlive()){
            try {
                listenThread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void dropClient(TCPHelper tcpClient){
        if(tcpClient.isOpen()){
            if(acceptSocketList.contains(tcpClient)){
                acceptSocketList.remove(tcpClient);
            }
            tcpClient.stopReceiveData();
            tcpClient.closeSocket();
        }
    }
    public void dropClient(String remoteIp, int remotePort){
        for (TCPHelper tcpClient:acceptSocketList
             ) {
            if(tcpClient.getRemoteIP().equals(remoteIp) &&
                    tcpClient.getRemotePort()==remotePort){
                dropClient(tcpClient);
            }
        }
    }

    public void dropAllClient(){
        for (TCPHelper tcpClient : acceptSocketList
        ) {
            dropClient(tcpClient);
        }
    }

    public void sendToAll(String data) {
        if (data.isEmpty() || acceptSocketList.size() == 0) return;
        for (TCPHelper tcp:acceptSocketList
        ) {
            tcp.send(data);
        }
    }

    public void sendTo(String remoteIp, int remotePort, String data) {
        if (data.isEmpty() || acceptSocketList.size() == 0) return;
        for (TCPHelper tcpClient:acceptSocketList
        ) {
            if(tcpClient.getRemoteIP().equals(remoteIp) &&
                    tcpClient.getRemotePort()==remotePort){
                tcpClient.send(data);
            }
        }
    }

    public void setOnAcceptListener(OnAcceptListener listener) {
        onAcceptListener = listener;
    }

    public interface OnAcceptListener {
        void onAccepted(TCPHelper tcpClient);
    }


}
