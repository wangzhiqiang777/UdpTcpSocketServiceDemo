package com.neusoft.qiangzi.socketservicedemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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

    ISocketBinder socketBinder;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            socketBinder = (ISocketBinder) iBinder;
            //初始化设置参数
            try {
                socketBinder.registerListener(receivedListener);
                socketBinder.setRemoteIP(etRemoteIP.getText().toString());
                socketBinder.setRemotePort(Integer.parseInt(etRemotePort.getText().toString()));
                socketBinder.setLocalPort(Integer.parseInt(etLocalPort.getText().toString()));
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

        etRemoteIP.setInputType(InputType.TYPE_NULL);
        etRemotePort.setInputType(InputType.TYPE_NULL);
        etLocalPort.setInputType(InputType.TYPE_NULL);
        etRecieveText.setInputType(InputType.TYPE_NULL);

        etRemoteIP.setOnClickListener(this);
        etRemotePort.setOnClickListener(this);
        etLocalPort.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnClearReceive.setOnClickListener(this);

        swUdp.setOnCheckedChangeListener(this);
        swTcp.setOnCheckedChangeListener(this);
        swTcpServer.setOnCheckedChangeListener(this);
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
                }
                break;
            case R.id.buttonClearReceive:
                etRecieveText.setText("");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(this, SocketService.class);
        bindService(i, connection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        socketBinder = null;
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
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
        }
    }
}