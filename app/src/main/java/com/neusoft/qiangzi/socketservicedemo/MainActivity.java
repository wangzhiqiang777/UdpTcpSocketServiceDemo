package com.neusoft.qiangzi.socketservicedemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.ServerSocket;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    EditText etRemoteIP;
    EditText etRemotePort;
    EditText etSendText;
    EditText etLocalPort;
    EditText etRecieveText;
    Button btnSend;
    Button btnClearReceive;
    Switch swUdp, swTcp,swTcpServer;
    ToggleButton btStartService;

    ISocketBinder socketBinder;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: is called.");
            socketBinder = (ISocketBinder) iBinder;
            if(socketBinder==null){
                Toast.makeText(MainActivity.this,"服务连接失败！",Toast.LENGTH_SHORT).show();
                return;
            }
            //初始化设置参数
            try {
                socketBinder.registerListener(receivedListener);
                //恢复状态
                swUdp.setChecked(socketBinder.isUDPEnabled());
                swTcp.setChecked(socketBinder.isTCPEnabled());
                swTcpServer.setChecked(socketBinder.isTCPServerEnabled());
                etLocalPort.setText(String.valueOf(socketBinder.getLocalPortPort()));
                etRemoteIP.setText(socketBinder.getRemoteIP());
                etRemotePort.setText(String.valueOf(socketBinder.getRemotePort()));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    IOnSocketReceivedListener receivedListener = new IOnSocketReceivedListener.Stub() {
        @Override
        public void onReceived(String data) throws RemoteException {
            etRecieveText.append("\n"+data);
        }

        @Override
        public void onEvent(int e) throws RemoteException {
            switch (e){
                case IOnSocketReceivedListener.OPEN_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.event_msg_open_uccess,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.CLOSE_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.event_msg_close_success,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.ACCEPT_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.event_msg_access_success,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.OPEN_FAILED:
                    Toast.makeText(MainActivity.this, R.string.event_msg_open_error,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.OPEN_TIMEOUT:
                    Toast.makeText(MainActivity.this, R.string.event_msg_open_timeout,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.BREAK_OFF:
                    Toast.makeText(MainActivity.this, R.string.event_msg_network_break_off,Toast.LENGTH_SHORT).show();
                    if(swTcp.isChecked())swTcp.setChecked(false);
                    break;
                case IOnSocketReceivedListener.SEND_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_send_error,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.RECV_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_receive_error,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.ACCEPT_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_client_accept_error,Toast.LENGTH_SHORT).show();
                    break;
                case IOnSocketReceivedListener.UNKNOWN_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_unknown_error,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etRemoteIP = findViewById(R.id.etRemoteIP);
        etRemotePort = findViewById(R.id.etRemotePort);
        etSendText = findViewById(R.id.etSendText);
        etLocalPort = findViewById(R.id.etLocalPort);
        etRecieveText = findViewById(R.id.etReceiveText);
        btnSend = findViewById(R.id.buttonSend);
        btnClearReceive = findViewById(R.id.buttonClearReceive);
        swUdp = findViewById(R.id.switchUdp);
        swTcp = findViewById(R.id.switchTcp);
        swTcpServer = findViewById(R.id.switchTcpServer);
        btStartService = findViewById(R.id.toggleButtonStartService);

        etRemoteIP.setInputType(InputType.TYPE_NULL);
        etRemotePort.setInputType(InputType.TYPE_NULL);
        etLocalPort.setInputType(InputType.TYPE_NULL);
        etRecieveText.setInputType(InputType.TYPE_NULL);
        etRecieveText.setSingleLine(false);

        etRemoteIP.setOnClickListener(this);
        etRemotePort.setOnClickListener(this);
        etLocalPort.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnClearReceive.setOnClickListener(this);

        swUdp.setOnCheckedChangeListener(this);
        swTcp.setOnCheckedChangeListener(this);
        swTcpServer.setOnCheckedChangeListener(this);
        btStartService.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isServiceRunning(ISocketBinder.SERVICE_NAME)){
            btStartService.setChecked(true);
            Intent i = new Intent(this, SocketService.class);
            bindService(i, connection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(socketBinder!=null) {
            try {
                socketBinder.unregisterListener(receivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(connection);
            socketBinder = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.etRemoteIP:
                inputTitleDialog(view.getId(),etRemoteIP.getText().toString());
                break;
            case R.id.etRemotePort:
                inputTitleDialog(view.getId(),etRemotePort.getText().toString());
                break;
            case R.id.etLocalPort:
                inputTitleDialog(view.getId(),etLocalPort.getText().toString());
                break;
            case R.id.buttonSend:
                if(socketBinder!=null){
                    try {
                        socketBinder.sendText(etSendText.getText().toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(this,"请开启服务！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonClearReceive:
                etRecieveText.setText("");
                break;
        }
    }


    private void inputTitleDialog(final int viewId, String currentText) {

        final EditText etInput = new EditText(this);
        etInput.setFocusable(true);
        etInput.setText(currentText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入")
                .setView(etInput)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(socketBinder == null){
                            Toast.makeText(MainActivity.this,"没有链接到服务！",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String input = etInput.getText().toString();
                        try {
                            switch (viewId){
                                case R.id.etRemoteIP:
                                    socketBinder.setRemoteIP(input);
                                    etRemoteIP.setText(input);
                                    break;
                                case R.id.etRemotePort:
                                    socketBinder.setRemotePort(Integer.parseInt(input));
                                    etRemotePort.setText(input);
                                    break;
                                case R.id.etLocalPort:
                                    socketBinder.setLocalPort(Integer.parseInt(input));
                                    etLocalPort.setText(input);
                                    break;
                            }
                            Toast.makeText(MainActivity.this,"设置成功！",Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"设置失败！",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(!compoundButton.isPressed())return;
        if(compoundButton.getId()!=R.id.toggleButtonStartService && socketBinder==null){
            Toast.makeText(this,"请开启服务！",Toast.LENGTH_SHORT).show();
            compoundButton.setChecked(false);
            return;
        }
        switch (compoundButton.getId()){
            case R.id.switchUdp:
                try {
                    socketBinder.setUDPEnabled(b);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.switchTcp:
                try {
                    socketBinder.setTCPEnabled(b);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.switchTcpServer:
                try {
                    socketBinder.setTCPServerEnabled(b);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.toggleButtonStartService:
                Intent i = new Intent(this, SocketService.class);
                if(b){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(i);
                    }else {
                        startService(i);
                    }
                    bindService(i, connection, BIND_AUTO_CREATE);
                }else {
                    if(socketBinder!=null) {
                        try {
                            socketBinder.unregisterListener(receivedListener);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        unbindService(connection);
                    }
                    stopService(i);
                    socketBinder = null;
                    swUdp.setChecked(false);
                    swTcp.setChecked(false);
                    swTcpServer.setChecked(false);
                }
                break;
        }
    }
    boolean isServiceRunning(String serviceName){
        // 校验服务是否还存在
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo info : services) {
            // 得到所有正在运行的服务的名称
            String name = info.service.getClassName();
            if (serviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }
}