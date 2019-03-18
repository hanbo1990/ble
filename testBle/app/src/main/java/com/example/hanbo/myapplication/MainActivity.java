package com.example.hanbo.myapplication;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maigantech.debuglib.Logger;
import com.mgtech.lib.blelib.BleConstants;
import com.mgtech.lib.blelib.BleCore;
import com.mgtech.lib.blelib.BleListener;

import java.util.Arrays;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements BleListener{

    private final static String TAG = "MainActivity";

    private Handler mHandler;
    private BleCore mBleCore;


    private Button btn_scan;
    private Button btn_pair;
    private Button btn_discover_service;
    private Button btn_pair_confirm;
    private Button btn_unpair;
    Button btn_disconnect;

    private TextView tv_status;
    private TextView tv_pair;
    EditText et_name;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        mBleCore = BleCore.getBLECore(this,this);

        et_name = (EditText)findViewById(R.id.et_devname);

        btn_scan = (Button)findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleCore.startLeScan();
                name = et_name.getText().toString();
            }
        });

        btn_pair = (Button)findViewById(R.id.btn_pair);
        btn_pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleCore.pair();
//                  mBleCore.getPairedDevices();
            }
        });

        btn_unpair = (Button)findViewById(R.id.btn_unpair);
        btn_unpair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bondedDevices = mBleCore.getPairedDevices();
                if(!bondedDevices.isEmpty())
                    for(BluetoothDevice mbluetoothdev: bondedDevices)
                        mBleCore.removePairInformation(mbluetoothdev.getAddress());
            }
        });

        btn_pair_confirm = (Button)findViewById(R.id.btn_pair_confirm);
        btn_pair_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                byte[] data = {1,1,1,1,1,5,5,5,5,5,6,6,6,6,6,1,1,1,1,1};
//                Logger.e(TAG,"write");
//                mBleCore.write(BleConstants.MG_PROFILE_SERVICE, BleConstants.BT_CHAR_CONF, data);
                mBleCore.toggleNotification(RemoteBluetoothConfig.MG_PROFILE_SERVICE,RemoteBluetoothConfig.BT_CHAR_DATA,true);
            }
        });

        btn_discover_service = (Button)findViewById(R.id.btn_discover_service);
        btn_discover_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleCore.discoverServices();
            }
        });

        btn_disconnect = (Button)findViewById(R.id.btn_disconnect);
        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBleCore.disconnect();
            }
        });

        tv_status = (TextView)findViewById(R.id.tv_status);
        tv_pair = (TextView)findViewById(R.id.tv_passkey);
    }

    @Override
    public void onScanResults(BluetoothDevice device, int rssi, byte[] bytes) {

        if(bytes[9] == 66){
//        if(device.getName() != null && device.getName().equals("HB")){
            mBleCore.setDeviceAddress(device.getAddress());
            mBleCore.connect();

        }
    }

    @Override
    public void onConnected(String address) {
        mBleCore.stopLeScan();
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_status.setText("已链接");
            }
        });
    }

    @Override
    public void onDisConnected() {
        Logger.e(TAG,"Disconnected");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_status.setText("已断开");
            }
        });
    }

    @Override
    public void onDisConnectedByPeer() {
        Logger.e(TAG,"Peer disconnect");
    }

    @Override
    public void onTimeOut() {
        Logger.e(TAG,"time out");
    }

    @Override
    public void onServiceDiscovered() {
        Logger.e(TAG,"Service Discovered");
    }

    @Override
    public void onWrite(boolean status, String s) {
        if(status == BleConstants.BLE_SUCCESS)
            Logger.e(TAG,"write success");
        else
            Logger.e(TAG,"write failed");
    }

    @Override
    public void onRead(boolean status, String s, byte[] value) {
        Logger.e(TAG,"onread"+ Arrays.toString(value) +"");
    }

    @Override
    public void onDataTransferEnabled() {
        Logger.e(TAG,"onDataTransferEnabled");
    }

    @Override
    public void onDataReceived(byte[] receivedData) {
        Logger.e(TAG,"onDataReceived");
    }

    @Override
    public void onRRSIChanged(int rssi) {
        Logger.e(TAG,"onRRSIChanged" + (rssi));
    }

    @Override
    public void onError(int Error) {
        Logger.e(TAG,"Error received: " + Error);
    }

    @Override
    public void onPairStatusChanged(boolean isPaired) {
        Logger.e(TAG,"pair status is : "+ isPaired);
    }

    @Override
    public void onPairRequest(int PairType) {
        Logger.e(TAG,"onPairRequest  received with type:" +PairType);
    }


}
