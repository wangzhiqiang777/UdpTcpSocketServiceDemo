package com.neusoft.qiangzi.socketservicedemo.SocketHelper;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDPHelper {
    private final String TAG = "UDPHelper";
    private int LocalPort = 7001;
    private int RemotePort = 7001;
    private String RemoteIP = "127.0.0.1";
    private DatagramSocket mSocket;
    private OnUDPReceiveListener Listener;
    private Thread receiveThread;
    private InetAddress iaRemoteIP = null;
    private boolean isOpened = false;
    private boolean isStartRecv = false;
    public String ReceivedMsg;
    public Object apiThreadLock = new Object();

    public UDPHelper() {
    }

    public void setLocalPort(int Port) {
        this.LocalPort = Port;
        Log.d(TAG, "setLocalPort: port=" + Port);
    }

    public int getLocalPortPort() {
        return LocalPort;
    }

    public boolean setRemoteIP(String ip) {
        if (isIP(ip)) {
            RemoteIP = ip;
            Log.d(TAG, "setRemoteIP: ip=" + ip);
            return true;
        } else return false;
    }

    public void setRemotePort(int port) {
        RemotePort = port;
        Log.d(TAG, "setRemotePort: port=" + port);
    }

    public int getRemotePort() {
        return RemotePort;
    }

    public boolean isOpen() {
        return isOpened;
    }

    public synchronized void openSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (apiThreadLock) {
                    try {
                        if (isOpened) {
                            return;
                        }
                        mSocket = new DatagramSocket(LocalPort);
                        mSocket.setSoTimeout(1000);//timeout for read
                        isOpened = true;
                    } catch (Exception e) {
                        mSocket = null;
                        isOpened = false;
                        Log.e(TAG, "openSocket error.e=" + e.toString());
                    }
                }
            }
        }).start();

    }

    public void send(final String data) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    mSocket = new DatagramSocket();
//                    mSocket.connect(InetAddress.getByName(RemoteIP), RemotePort);
                    if (mSocket == null || mSocket.isClosed()) {
                        Log.e(TAG, "run: socket is inavilable");
                        return;
                    }

                    Log.d(TAG, "send:" + data);
                    byte[] datas = data.getBytes();
                    final DatagramPacket packet = new DatagramPacket(datas, datas.length, InetAddress.getByName(RemoteIP), RemotePort);
                    mSocket.send(packet);
                } catch (Exception e) {
                    Log.e(TAG, "send error.e=" + e.toString());
                    return;
                }
            }
        });
        t.start();
    }

    public void setOnUDPReceiveListener(OnUDPReceiveListener listener) {
        Listener = listener;
    }

    public String getRemoteIP() {
        if (iaRemoteIP == null) return "";
        return iaRemoteIP.getHostAddress();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                if (Listener != null) Listener.onReceived(ReceivedMsg);
            }
        }
    };

    public synchronized void startReceiveData() {
        if (isStartRecv) return;
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (apiThreadLock) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mSocket == null || mSocket.isClosed()) {
                        Log.e(TAG, "run: socket is inavilable");
                        return;
                    }
                    Log.d(TAG, "startReceiveData ok.");

                    isStartRecv = true;
                    byte[] datas = new byte[512];
                    //DatagramPacket packet = new DatagramPacket(datas, datas.length, null, LocalPort);
                    DatagramPacket packet = new DatagramPacket(datas, datas.length);

                    while (isStartRecv) {
                        try {
                            mSocket.receive(packet);
                            iaRemoteIP = packet.getAddress();
                            ReceivedMsg = new String(packet.getData()).trim();
                            mHandler.sendEmptyMessage(100);
                            java.util.Arrays.fill(datas, (byte) 0);
                            Log.d(TAG, "recv:(" + iaRemoteIP.getHostAddress() + ")" + ReceivedMsg);
                        } catch (SocketTimeoutException e) {
                            //超时，继续接受
                            Log.d(TAG, "receiveThread: is waiting...");
                        } catch (Exception e) {
                            Log.e(TAG, "startReceiveData error.e=" + e.toString());
                        }
                    }
                    isStartRecv = false;
                    Log.d(TAG, "receiveThread: end!");
                }
            }
        });
        receiveThread.start();
    }

    public synchronized void stopReceiveData() {
        if (!isStartRecv) return;
        isStartRecv = false;
        try {
            if (receiveThread != null && receiveThread.isAlive()) {
                receiveThread.join();
                receiveThread = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "stopReceiveData error.e=" + e.toString());
        }
    }

    public void closeSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (apiThreadLock) {
                    try {
                        if (isStartRecv) {
                            stopReceiveData();
                        }
                        if (mSocket != null && !mSocket.isClosed()) {
                            mSocket.close();
                            mSocket = null;
                        }
                        isOpened = false;
                        Log.d(TAG, "closeSocket: ok");
                    } catch (Exception e) {
                        Log.e(TAG, "closeSocket error.e=" + e.toString());
                    }
                }
            }
        }).start();
    }

    public void restartReceiveData() {
        new Thread() {
            @Override
            public void run() {
                synchronized (apiThreadLock) {
                    //停止接收线程
                    if (isStartRecv) {
                        isStartRecv = false;
                        try {
                            if (receiveThread != null && receiveThread.isAlive()) {
                                receiveThread.join();
                                receiveThread = null;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "stopReceiveData error.e=" + e.toString());
                        }
                    }
                }
                closeSocket();
                openSocket();
                startReceiveData();
            }
        }.start();
    }

    public static boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
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
        if (ipAddress == true) {
            String ips[] = addr.split("\\.");

            if (ips.length == 4) {
                try {
                    for (String ip : ips) {
                        if (Integer.parseInt(ip) < 0 || Integer.parseInt(ip) > 255) {
                            return false;
                        }

                    }
                } catch (Exception e) {
                    return false;
                }

                return true;
            } else {
                return false;
            }
        }

        return ipAddress;
    }

    public interface OnUDPReceiveListener {
        void onReceived(String data);
    }

}
