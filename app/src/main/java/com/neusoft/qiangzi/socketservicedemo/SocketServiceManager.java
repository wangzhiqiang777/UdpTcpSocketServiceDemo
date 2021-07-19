package com.neusoft.qiangzi.socketservicedemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.neusoft.qiangzi.socketservicedemo.utils.ServiceUtil;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.os.Build.VERSION.SDK_INT;

public class SocketServiceManager extends ISocketBinder.Stub{
    private static final String TAG = "SocketServiceManager";
    public static final String SERVICE_NAME = "com.neusoft.qiangzi.socketservicedemo.SocketService";
    public static final String SERVICE_PACKEG = "com.neusoft.qiangzi.socketservicedemo";
    private Context context;
    private ISocketBinder binder;
    private ISocketListener listener;
    private OnBindedListener onBindedListener;
    private boolean isBinded = false;
    private static SocketServiceManager instance;

    public SocketServiceManager(Context context) {
        this.context = context;
    }

    public static SocketServiceManager getInstance(Context context) {
        if (instance == null) {
            instance = new SocketServiceManager(context);
        }
        return instance;
    }
    public void start() {
        //如果服务没有启动，则启动服务
        if (!ServiceUtil.isServiceRunning(context, SERVICE_NAME)) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(SERVICE_PACKEG, SERVICE_NAME));
            if (SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            }else {
                context.startService(i);
            }
        }
    }

    public void stop() {
        //如果服务没有启动，则启动服务
        if (ServiceUtil.isServiceRunning(context, SERVICE_NAME)) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(SERVICE_PACKEG, SERVICE_NAME));
            context.stopService(i);
        }
    }

    public boolean isStared() {
        return ServiceUtil.isServiceRunning(context, SERVICE_NAME);
    }

    public void setOnBindedListener(OnBindedListener listener) {
        onBindedListener = listener;
    }

    public void setSocketListener(ISocketListener listener) {
        this.listener = listener;
    }
    public void bind() {
        if(isBinded)return;
        if (!isStared()) {
            start();
        }
        //绑定服务
        Intent i = new Intent();
        i.setComponent(new ComponentName(SERVICE_PACKEG, SERVICE_NAME));
        context.bindService(i, connection, BIND_AUTO_CREATE);
    }

    public void unbind() {
        if(!isBinded) return;
        if (binder != null) {
            try {
                binder.unregisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //解绑服务
        if (context != null) {
            context.unbindService(connection);
        }
        isBinded = false;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: is called");
            isBinded = true;
            binder = ISocketBinder.Stub.asInterface(iBinder);
            if (binder != null) {
                try {
                    binder.registerListener(listener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if(onBindedListener !=null) onBindedListener.onBinded();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: is called");
            binder = null;
            isBinded = false;
        }
    };

    interface OnBindedListener {
        void onBinded();
    }

    ///////////以下为接口原有方法的实现//////////////

    @Override
    public void setUDPEnabled(boolean enabled) {
        if (binder != null) {
            try {
                binder.setUDPEnabled(enabled);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "setUDPEnabled: binder is null");
        }
    }

    @Override
    public void setTCPEnabled(boolean enabled) {
        if (binder != null) {
            try {
                binder.setTCPEnabled(enabled);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "setTCPEnabled: binder is null");
        }
    }

    @Override
    public void setTCPServerEnabled(boolean enabled) {
        if (binder != null) {
            try {
                binder.setTCPServerEnabled(enabled);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "setTCPServerEnabled: binder is null");
        }
    }

    @Override
    public boolean isUDPEnabled() {
        if (binder != null) {
            try {
                return binder.isUDPEnabled();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "isUDPEnabled: binder is null");
        }
        return false;
    }

    @Override
    public boolean isTCPEnabled() {
        if (binder != null) {
            try {
                return binder.isTCPEnabled();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "isTCPEnabled: binder is null");
        }
        return false;
    }

    @Override
    public boolean isTCPServerEnabled() {
        if (binder != null) {
            try {
                return binder.isTCPServerEnabled();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "isTCPServerEnabled: binder is null");
        }
        return false;
    }

    @Override
    public void setLocalPort(int Port) {
        if (binder != null) {
            try {
                binder.setLocalPort(Port);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "setLocalPort: binder is null");
        }
    }

    @Override
    public int getLocalPortPort() {
        if (binder != null) {
            try {
                return binder.getLocalPortPort();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "getLocalPortPort: binder is null");
        }
        return 0;
    }

    @Override
    public void setRemoteIP(String ip) {
        if (binder != null) {
            try {
                binder.setRemoteIP(ip);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "setRemoteIP: binder is null");
        }
    }

    @Override
    public String getRemoteIP() {
        if (binder != null) {
            try {
                return binder.getRemoteIP();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "getRemoteIP: binder is null");
        }
        return null;
    }

    @Override
    public void setRemotePort(int port) {
        if (binder != null) {
            try {
                binder.setRemotePort(port);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "setRemotePort: binder is null");
        }
    }

    @Override
    public int getRemotePort() {
        if (binder != null) {
            try {
                return binder.getRemotePort();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "getRemotePort: binder is null");
        }
        return 0;
    }

    @Override
    public void sendText(String text) {
        if (binder != null) {
            try {
                binder.sendText(text);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "sendText: binder is null");
        }
    }

    @Override
    public void connect(String remoteIp, int remotePort) {
        if (binder != null) {
            try {
                binder.connect(remoteIp, remotePort);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "connect: binder is null");
        }
    }

    @Override
    public void disconnect(String remoteIp, int remotePort) {
        if (binder != null) {
            try {
                binder.disconnect(remoteIp, remotePort);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "disconnect: binder is null");
        }
    }

    @Override
    public void registerListener(ISocketListener listener) {
        if (binder != null) {
            try {
                binder.registerListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "registerListener: binder is null");
        }
    }

    @Override
    public void unregisterListener(ISocketListener listener) {
        if (binder != null) {
            try {
                binder.unregisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "unregisterListener: binder is null");
        }
    }
}
