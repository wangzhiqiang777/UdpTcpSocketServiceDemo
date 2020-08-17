package com.neusoft.qiangzi.socketservicedemo.SocketHelper;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDPHelper {
    private final String TAG = "UDPHelper";
    private int LocalPort = 7001;
    private int RemotePort = 7001;
    private String RemoteIP = "127.0.0.1";
    private DatagramSocket mSocket;
    private OnUDPReceiveListener Listener;
    private Thread ReceiveThread;
    private InetAddress iaRemoteIP =null;
    private boolean isOpened = false;

    public String ReceivedMsg;

    public UDPHelper(){}
    public void setLocalPort(int Port){
        this.LocalPort = Port;
    }
    public int getLocalPortPort() {
        return LocalPort;
    }
    public boolean setRemoteIP(String ip){
        if(isIP(ip)) {
            RemoteIP = ip;
            return true;
        }else return false;
    }
    public void setRemotePort(int port){
        RemotePort = port;
    }
    public int getRemotePort() {
        return RemotePort;
    }
    public boolean isOpen(){return isOpened;}

    public void openSocket(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(isOpened){
                        return;
                    }
                    mSocket = new DatagramSocket(LocalPort);
                    isOpened = true;
                }catch (Exception e){
                    mSocket =null;
                    isOpened = false;
                    Log.e(TAG,"openSocket error.e="+e.toString());
                }
            }
        }).start();

    }
    public void send(final String data){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    mSocket = new DatagramSocket();
//                    mSocket.connect(InetAddress.getByName(RemoteIP), RemotePort);
                    if(mSocket ==null || mSocket.isClosed())return;

                    Log.d(TAG, "send:"+data);
                    byte[]datas = data.getBytes();
                    final DatagramPacket packet = new DatagramPacket(datas, datas.length, InetAddress.getByName(RemoteIP), RemotePort);
                    mSocket.send(packet);
                }catch (Exception e){
                    Log.e(TAG,"send error.e="+e.toString());
                    return;
                }
            }
        });
        t.start();
    }

    public void setOnUDPReceiveListener(OnUDPReceiveListener listener){
        Listener = listener;
    }
    public String getRemoteIP(){
        if(iaRemoteIP ==null)return "";
        return iaRemoteIP.getHostAddress();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                if(Listener !=null) Listener.onReceived(ReceivedMsg);
            }
        }
    };

    public void startReceiveData(){
        ReceiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //mSocket = new DatagramSocket(LocalPort);
                    Thread.sleep(100);

                    if(mSocket ==null)return;
                    Log.d(TAG,"startReceiveData ok.");

                    byte[]datas = new byte[512];
                    //DatagramPacket packet = new DatagramPacket(datas, datas.length, null, LocalPort);
                    DatagramPacket packet = new DatagramPacket(datas, datas.length);

                    while (true){
                        mSocket.receive(packet);
                        iaRemoteIP = packet.getAddress();
                        ReceivedMsg = new String(packet.getData()).trim();
                        mHandler.sendEmptyMessage(100);
                        java.util.Arrays.fill(datas, (byte) 0);
                        Log.d(TAG, "recv:("+ iaRemoteIP.getHostAddress()+")"+ ReceivedMsg);
                    }
                }catch (Exception e){
                    Log.e(TAG,"startReceiveData error.e="+e.toString());
                }
            }
        });
        ReceiveThread.start();
    }
    public void stopReceiveData(){
        try{
            if(ReceiveThread !=null && !ReceiveThread.isInterrupted()) {
                //ReceiveThread.interrupt();
                //ReceiveThread.join();
                ReceiveThread =null;
            }
        }catch (Exception e){
            Log.e(TAG,"stopReceiveData error.e="+e.toString());
        }
    }
    public void closeSocket(){
        //stopReceiveData();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(mSocket !=null && !mSocket.isClosed()){
                        mSocket.close();
                        mSocket =null;
                        ReceiveThread =null;
                    }
                    isOpened = false;
                }catch (Exception e){
                    Log.e(TAG,"closeSocket error.e="+e.toString());
                }
            }
        }).start();
    }


    public static boolean isIP(String addr)
    {
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr))
        {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        //============对之前的ip判断的bug在进行判断
        if (ipAddress==true){
            String ips[] = addr.split("\\.");

            if(ips.length==4){
                try{
                    for(String ip : ips){
                        if(Integer.parseInt(ip)<0|| Integer.parseInt(ip)>255){
                            return false;
                        }

                    }
                }catch (Exception e){
                    return false;
                }

                return true;
            }else{
                return false;
            }
        }

        return ipAddress;
    }
    public interface OnUDPReceiveListener {
        void onReceived(String data);
    }

}
