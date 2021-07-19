package com.neusoft.qiangzi.socketservicedemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    SocketServiceManager serviceManager;

    SocketServiceManager.OnBindedListener bindedListener = new SocketServiceManager.OnBindedListener() {
        @Override
        public void onBinded() {
            //初始化设置参数
            serviceManager.registerListener(socketListener);
            //恢复状态
            swUdp.setChecked(serviceManager.isUDPEnabled());
            swTcp.setChecked(serviceManager.isTCPEnabled());
            swTcpServer.setChecked(serviceManager.isTCPServerEnabled());
            etLocalPort.setText(String.valueOf(serviceManager.getLocalPortPort()));
            etRemoteIP.setText(serviceManager.getRemoteIP());
            etRemotePort.setText(String.valueOf(serviceManager.getRemotePort()));
        }
    };
    
    ISocketListener socketListener = new ISocketListener.Stub() {
        @Override
        public void onReceived(String data) throws RemoteException {
            etRecieveText.append("\n"+data);
        }
        @Override
        public void onEvent(int e) throws RemoteException {
            switch (e){
                case ISocketListener.OPEN_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.event_msg_open_uccess,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.CLOSE_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.event_msg_close_success,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.ACCEPT_SUCCESS:
                    Toast.makeText(MainActivity.this, R.string.event_msg_access_success,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.OPEN_FAILED:
                    Toast.makeText(MainActivity.this, R.string.event_msg_open_error,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.OPEN_TIMEOUT:
                    Toast.makeText(MainActivity.this, R.string.event_msg_open_timeout,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.BREAK_OFF:
                    Toast.makeText(MainActivity.this, R.string.event_msg_network_break_off,Toast.LENGTH_SHORT).show();
                    if(swTcp.isChecked())swTcp.setChecked(false);
                    break;
                case ISocketListener.SEND_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_send_error,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.RECV_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_receive_error,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.ACCEPT_ERROR:
                    Toast.makeText(MainActivity.this, R.string.event_msg_client_accept_error,Toast.LENGTH_SHORT).show();
                    break;
                case ISocketListener.UNKNOWN_ERROR:
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
        //启动服务
        serviceManager = new SocketServiceManager(this);
        serviceManager.setOnBindedListener(bindedListener);
        serviceManager.setSocketListener(socketListener);
        serviceManager.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceManager.bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serviceManager.unbind();
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
                if(serviceManager.isStared()){
                    serviceManager.sendText(etSendText.getText().toString());
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
                        if (!serviceManager.isStared()) {
                            Toast.makeText(MainActivity.this, "没有链接到服务！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String input = etInput.getText().toString();
                        switch (viewId) {
                            case R.id.etRemoteIP:
                                serviceManager.setRemoteIP(input);
                                etRemoteIP.setText(input);
                                break;
                            case R.id.etRemotePort:
                                serviceManager.setRemotePort(Integer.parseInt(input));
                                etRemotePort.setText(input);
                                break;
                            case R.id.etLocalPort:
                                serviceManager.setLocalPort(Integer.parseInt(input));
                                etLocalPort.setText(input);
                                break;
                        }
                        Toast.makeText(MainActivity.this, "设置成功！", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(!compoundButton.isPressed())return;
        if(compoundButton.getId()!=R.id.toggleButtonStartService && !serviceManager.isStared()){
            Toast.makeText(this,"请开启服务！",Toast.LENGTH_SHORT).show();
            compoundButton.setChecked(false);
            return;
        }
        switch (compoundButton.getId()){
            case R.id.switchUdp:
                serviceManager.setUDPEnabled(b);
                break;
            case R.id.switchTcp:
                serviceManager.setTCPEnabled(b);
                break;
            case R.id.switchTcpServer:
                serviceManager.setTCPServerEnabled(b);
                break;
            case R.id.toggleButtonStartService:
                Intent i = new Intent(this, SocketService.class);
                if(b){
                    serviceManager.start();
                    serviceManager.bind();
                }else {
                    serviceManager.unbind();
                    serviceManager.stop();
                    swUdp.setChecked(false);
                    swTcp.setChecked(false);
                    swTcpServer.setChecked(false);
                }
                break;
        }
    }

}