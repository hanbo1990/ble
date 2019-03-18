package com.mgtech.lib.blelib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.maigantech.debuglib.Logger;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Bo Han on 2015/9/15, last Edit by Bo Han 2016/6/29.
 * @author Bo Han
 */
public class BleCore implements BluetoothAdapter.LeScanCallback{

    private static final String TAG ="BleCore";

    private static boolean DBG = true;

    private static boolean ATUO_BOND = true;

    /** Interface indicating bluetooth status {@link BleListener}*/
    private static BleListener mBleListener;

    /** Interface to access bluetooth functions */
    private static BleCore mBleCore = null;

    /** Context to access bluetooth */
    private static Context mContext;

    /** Bluetooth connection status */
    private static int connectionStatus = BleConstants.STATE_DISCONNECTED;

    /** represents the local device adapter, in charge of basic ble tasks, scan, give devices....etc, {@link BluetoothAdapter} */
    private static BluetoothAdapter mBluetoothAdapter;

    /** Note: remote device, found in the ScanCallBack
       1. initialized:   mDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress); {@link BluetoothDevice}
    */
    private static BluetoothDevice mDevice;

    /** Address of remote device */
    private static String mDeviceAddress;

    /** Bluetooth GATT functionality to enable communication with Bluetooth Smart or Smart Ready devices
    *  get everything of dealing with Attritube table, like connect to remote device(GATT),
    *  1. initialized when GATT_SUCCESS, mBluetoothGatt=gatt; in gattcallback {@link BluetoothGatt}
    */
    private static BluetoothGatt mBluetoothGatt;

    /** GattCall back! {@link GATTCallBack}*/
    private static GATTCallBack mGattCallBack;

    /** Note: service in the gatt server of remote device
    * 1. initiated onServicesDiscovered {@link BleServiceStructure}
    */
    private static List<BleServiceStructure> mServices;

    /**
     *  Create and initialize BleCore instance.
     *  @param context Context using Bluetooth
     */
    private BleCore(Context context){
        mContext = context;
        getBleAdapter();

        mServices = new ArrayList<>();

        mGattCallBack = new GATTCallBack();
    }

    /**
     * prepare core elements/ set and get function
     * @param context Context using Bluetooth, {@see getApplicationContext}
     * @param listener Scanner implements the BleListener {@see BleListener
     * @return  mBleCore
     */
    public static BleCore getBLECore(Context context, BleListener listener){
        if(DBG) Logger.i(TAG,"getting BLE core");
        if(mBleCore == null){
            synchronized (BleCore.class){
                if (mBleCore == null){
                    mBleCore = new BleCore(context);
                    mBleListener = listener;
                }
            }
            // register broadcast events for pairing
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
            mContext.registerReceiver(mBondingBroadcastReceiver, intentFilter);
        }
        return mBleCore;
    }

    /** Called when device need a total reset, for example: connection unrecoverable error, disconnect..etc*/
    public static void resetBLECore(){
        if(DBG) Logger.i(TAG,"resetting BLE core");
        if(mServices != null) mServices.clear();
        if(mDevice != null) mDevice = null;
        if(mDeviceAddress != null) mDeviceAddress = null;
        resetBluetoothGatt();
        getBleAdapter();
        connectionStatus = BleConstants.STATE_DISCONNECTED;
    }

    /**
     * Close the whole BLECore, called by outside when the BLECore is not needed any more
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!don't forget
     */
    public static void closeTheWholeBleCore(){
        if(DBG) Logger.i(TAG,"closing the BLE core");
        resetBluetoothGatt();

        if(mServices != null) mServices = null;
        if(mBleListener != null) mBleListener = null;
        if(mBluetoothAdapter != null) mBluetoothAdapter = null;

        mContext.unregisterReceiver(mBondingBroadcastReceiver);

        mBleCore = null;
    }



    /**
     * internal gattcall back should resetBluetoothGatt after external disconnect
     * this is must for continuously scan!!!!!!!!!!!!!!! or no onLeScan for ever
     * only serveral ConnectedGatt are supported , after that no scan result will be
     * given, so should disconnect gatt and close it!
     */
    private static void resetBluetoothGatt(){
        if(DBG) Logger.i(TAG,"resetting GATT ");
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /** Get Bluetooth Adapter */
    private static void getBleAdapter(){
        if(DBG) Logger.i(TAG,"getting BluetoothAdapter");
        if(mBluetoothAdapter == null) {
            BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    // **********************************************************************************************
    // **************************    BLE Base Functions    ******************************************  BLE Base Functions
    // **********************************************************************************************

    /** Start Scan, result will appear @ onScanResults in {@link BleListener} */
    public void startLeScan(){
        if(mBluetoothAdapter == null) {
            if(DBG) Logger.i(TAG,"starting scan failed due to null adapter");
            mBleListener.onError(BleConstants.BLE_ERR_NULL_ADAPTER);
            return;
        }else if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_NONE){
            if(DBG) Logger.i(TAG,"starting scan failed due to abnormal scan status");
            mBleListener.onError(BleConstants.BLE_ERR_SCAN_ABNORMAL);
            return;
        }
        if(DBG) Logger.i(TAG,"starting scan success");
        mBluetoothAdapter.startLeScan(this);
    }

    /** Stop Scan */
    public void stopLeScan() {
        if(DBG) Logger.i(TAG,"stopping scan success");
        mBluetoothAdapter.stopLeScan(this);
    }

    /**
     * Show existing bonded devices
     * @return Set<BluetoothDevice> return the bondedDevice set
     */
    public Set<BluetoothDevice> getPairedDevices(){
        if(DBG) Logger.i(TAG,"getting bonded devices");
        return mBluetoothAdapter.getBondedDevices();
    }


    /** Connect to the remote device and register GATTCallBack */
    public void connect(){
        if (DBG) Logger.i(TAG, "trying to connect" );
//        if(connectionStatus == BleConstants.STATE_DISCONNECTED) {
            if(DBG) Logger.i(TAG,"connecting");
            mDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
            mServices.clear();
            connectionStatus = BleConstants.STATE_CONNECTING;
            if (mBluetoothGatt != null) {
                mDevice.connectGatt(mContext, false, mGattCallBack);
            } else {
                mDevice.connectGatt(mContext, false, mGattCallBack);
            }
//        }
    }

    /** Pair the connected device, createBond need no connection */
    public void pair(){
        if(connectionStatus != BleConstants.STATE_CONNECTED){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }// todo if the other side forget
        if(mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            if(DBG) Logger.i(TAG,"creating bond");
            mDevice.createBond();
        }
//            mDevice.setPin(BleConstants.PAIR_PASSWD);
    }

    /** Pair the connected device, createBond need no connection */
    public void removePairInformation(String bluetoothAddress){
        BluetoothDevice targetDevice = mBluetoothAdapter.getRemoteDevice(bluetoothAddress);
        if(targetDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            if(DBG) Logger.i(TAG,"removing bond");
            try {
                Method createBondMethod = targetDevice.getClass().getMethod("removeBond");
                createBondMethod.invoke(targetDevice);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /** Enable auto pair */
    public void enableAutoPair(){
        ATUO_BOND = true;
    }

    /** disable auto pair */
    public void diableAutoPair(){
        ATUO_BOND = false;
    }

    /** Pair the connected device, createBond need no connection automatically */
    private static void autoPair() throws Exception{
        if(connectionStatus != BleConstants.STATE_CONNECTED){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        ClsUtils.setPin(mDevice.getClass(),mDevice,"000000");
        ClsUtils.createBond(mDevice.getClass(), mDevice);
        ClsUtils.cancelPairingUserInput(mDevice.getClass(), mDevice);
        mDevice.setPairingConfirmation(true);
    }

    /** Confirm pair request */
    public void ConfirmPair(){
        if (connectionStatus != BleConstants.STATE_CONNECTED) {
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        //mDevice.setPin(BleConstants.PAIR_PASSWD);
        //mDevice.setPairingConfirmation(true);
        if(DBG) Logger.i(TAG,"confirming pair");
        mDevice.setPairingConfirmation(true);
    }

    /** Call GATT function: discoverService() */
    public void discoverServices(){
        if (connectionStatus != BleConstants.STATE_CONNECTED) {
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        if(DBG) Logger.i(TAG,"discovering service");
        mBluetoothGatt.discoverServices();
    }

    /**
     * Call GATT function: readCharacteristic(BluetoothGattCharacteristic)
     * @param serviceUUID UUID of the service
     * @param characteristicUUID UUID of the target characteristic
     */
    public void read(String serviceUUID, String characteristicUUID){
        if(connectionStatus != BleConstants.STATE_CONNECTED){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        if(characteristic != null){
            int permission = characteristic.getProperties();
            if((permission & BleConstants.AT_BLE_PERMISSION_CHAR_READ)
                    == BleConstants.AT_BLE_PERMISSION_CHAR_READ ) {
                if(DBG) Logger.i(TAG,"reading characteristic");
                mBluetoothGatt.readCharacteristic(characteristic);
            }
            else {
                if(DBG) Logger.i(TAG,"read permission denied");
                mBleListener.onError(BleConstants.BLE_ERR_NO_READ_PERMISSION);
            }
        } else {
            if(DBG) Logger.i(TAG,"null characteristic");
            mBleListener.onError(BleConstants.BLE_ERR_CHARACTERISTIC_NOT_FOUND);
        }
    }

    /**
     * Call GATT function: writeCharacteristic(BluetoothGattCharacteristic)
     * @param serviceUUID UUID of the service
     * @param characteristicUUID UUID of the target characteristic
     */
    public void write(String serviceUUID, String characteristicUUID, byte[] data){
        if(connectionStatus != BleConstants.STATE_CONNECTED){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        if(characteristic != null){
            int permission = (byte)characteristic.getProperties();
            if((permission & BleConstants.AT_BLE_PERMISSION_CHAR_WRITE)
                    == BleConstants.AT_BLE_PERMISSION_CHAR_WRITE ||
                    (permission & BleConstants.AT_BLE_PERMISSION_CHAR_SIGNED_WRITE)
                            == BleConstants.AT_BLE_PERMISSION_CHAR_SIGNED_WRITE ||
                    (permission & BleConstants.AT_BLE_PERMISSION_CHAR_WRITE_WITHOUT_RESPONSE)
                            == BleConstants.AT_BLE_PERMISSION_CHAR_WRITE_WITHOUT_RESPONSE ||
                    (permission & BleConstants.AT_BLE_PERMISSION_CHAR_RELIABLE_WRITE)
                            == BleConstants.AT_BLE_PERMISSION_CHAR_RELIABLE_WRITE ) {
                if(DBG) Logger.i(TAG,"writing characteristic");
                characteristic.setValue(data);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
            else {
                if(DBG) Logger.i(TAG,"writing permission denied");
                mBleListener.onError(BleConstants.BLE_ERR_NO_WRITE_PERMISSION);
            }
        } else {
            if(DBG) Logger.i(TAG,"null characteristic");
            mBleListener.onError(BleConstants.BLE_ERR_CHARACTERISTIC_NOT_FOUND);
        }
    }


    public void toggleNotification(String serviceUUID, String characteristicUUID, boolean notificationFlag) {
        if(connectionStatus != BleConstants.STATE_CONNECTED){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        if(characteristic!=null){
            int permission = (byte)characteristic.getProperties();
            if((permission & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                UUID CCC = UUID.fromString(BleConstants.CONFIG_DESCRIPTOR);
                mBluetoothGatt.setCharacteristicNotification(characteristic, notificationFlag); //Enabled locally
                BluetoothGattDescriptor config = characteristic.getDescriptor(CCC);
                if (notificationFlag) {
                    config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    config.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(config); //Enabled remotely
            }else{
                if(DBG) Logger.i(TAG,"characteristic has no notification property");
                mBleListener.onError(BleConstants.BLE_ERR_NO_NOTIFY_PERMISSION);
            }
        } else {
            if(DBG) Logger.i(TAG,"null characteristic");
            mBleListener.onError(BleConstants.BLE_ERR_CHARACTERISTIC_NOT_FOUND);
        }
    }

    public void enableIndication(String serviceUUID, String characteristicUUID, boolean indicationFlag) {
        if(connectionStatus != BleConstants.STATE_CONNECTED){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUUID, characteristicUUID);
        if(characteristic!=null){
            int permission = (byte)characteristic.getProperties();
            if((permission & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
                UUID CCC = UUID.fromString(BleConstants.CONFIG_DESCRIPTOR);
                mBluetoothGatt.setCharacteristicNotification(characteristic, indicationFlag); //Enabled locally
                BluetoothGattDescriptor config = characteristic.getDescriptor(CCC);
                if (indicationFlag) {
                    config.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                } else {
                    config.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(config); //Enabled remotely
            }else{
                if(DBG) Logger.i(TAG,"characteristic has no indication property");
                mBleListener.onError(BleConstants.BLE_ERR_NO_INDICATION_PERMISSION);
            }
        } else {
            if(DBG) Logger.i(TAG,"characteristic not found in the remote device");
            mBleListener.onError(BleConstants.BLE_ERR_CHARACTERISTIC_NOT_FOUND);
        }
    }


    /** Call GATT fucntion: disconnect() */
    public static void disconnect() {
        if(connectionStatus == BleConstants.STATE_DISCONNECTED || connectionStatus == BleConstants.STATE_DISCONNECTING){
            if(DBG) Logger.i(TAG,"wrong operation, please connect first");
            mBleListener.onError(BleConstants.BLE_ERR_WRONG_OPERATION);
            return;
        }
        connectionStatus = BleConstants.STATE_DISCONNECTING;
        if(DBG) Logger.i(TAG,"Disconnecting...");
        mBluetoothGatt.disconnect();
    }

    /** Find Characteristic by UUDI from certain service */
    private BluetoothGattCharacteristic findCharacteristic(String serviceUUID, String characteristicUUID){
        if (connectionStatus != BleConstants.STATE_CONNECTED) {
            return null;
        }
        for (BleServiceStructure serviceInList : mServices) {
            if (serviceInList.getService().getUuid().toString().equalsIgnoreCase(serviceUUID) ){
                for (BluetoothGattCharacteristic characteristicInList : serviceInList.getCharacteristics()) {
                    if (characteristicInList.getUuid().toString().equalsIgnoreCase(characteristicUUID) ){
                        return characteristicInList;
                    }
                }
            }
        }
        return null;
    }

    public static int packageNr = 0;


    // **********************************************************************************************
    // **************************      Bonding Broadcast ********************************************     bonding broadcast
    // **********************************************************************************************
    /** Broadcast Receiver receiving bonding status */
    private static BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    if(DBG) Logger.i(TAG,"pair operation success");
                    mBleListener.onPairStatusChanged(BleConstants.BLE_SUCCESS);
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    if(DBG) Logger.i(TAG,"pair operation failed");
                    mBleListener.onPairStatusChanged(BleConstants.BLE_FAILED);
                }
            }
            if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)){
                if(DBG) Logger.i(TAG,"receiving pair request");
                if(ATUO_BOND){
                    try{
//                        autoPair();

                        mDevice.setPin(BleConstants.PASSWD.getBytes());
                        // this is for meizu!!!!!
                        mDevice.createBond();

//                        Method createBondMethod = mDevice.getClass().getMethod("cancelPairingUserInput");
                        mDevice.setPairingConfirmation(true);
//                        Boolean returnValue = (Boolean) createBondMethod.invoke(mDevice);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    mBleListener.onPairRequest(intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,BluetoothDevice.ERROR));
                }
            }
        }
    };



    // **********************************************************************************************
    // **************************      GATT CALLBACK     ********************************************     GATT CALLBACK
    // **********************************************************************************************

    /**
     *  Bluetooth GATTCallBack
     */
    private class GATTCallBack extends BluetoothGattCallback{
        GATTCallBack(){}

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status != 0) {
                if(DBG)Logger.e(TAG, "Error status received onConnectionStateChange: " + status + " - New state: " + newState);
            } else {
                if (DBG)Logger.w(TAG, "onConnectionStateChange received. status = " + status +
                        " - State: " + newState);
            }

            if ((status == BleConstants.GATT_ERROR)||(status == BleConstants.GAP_EVT_CONN_CLOSED)) {
                if(DBG)Logger.i(TAG, "Unrecoverable error 133 or 257.   full reset");
                mBleListener.onError(BleConstants.BLE_ERR_FATAL);
                resetBLECore();
                return;
            }

            if (newState== BluetoothProfile.STATE_CONNECTED && status==BluetoothGatt.GATT_SUCCESS){ //Connected
                if(DBG) Logger.i(TAG,"device connected");
                mBluetoothGatt=gatt;
                packageNr = 0;
                connectionStatus = BleConstants.STATE_CONNECTED;
                mBleListener.onConnected(mDeviceAddress);
                return;
            }

            if (newState==BluetoothProfile.STATE_DISCONNECTED && status==BluetoothGatt.GATT_SUCCESS){ //Disconnected
                if(DBG) Logger.i(TAG,"device disconnected");
                connectionStatus = BleConstants.STATE_DISCONNECTED;
                mBleListener.onDisConnected();
                return;
            }
            // these happenings appear after everything about ble are put into BleCore!!!!!!!!!!!!
            // connection time out status == 8
            if(newState==BluetoothProfile.STATE_DISCONNECTED && status==BleConstants.HCI_ERR_CONNECTION_TOUT){
                if(DBG) Logger.i(TAG,"connection timeout");
                mBleListener.onTimeOut();
                return;
            }
            // disconnected by peer user
            if(newState==BluetoothProfile.STATE_DISCONNECTED && status==BleConstants.HCI_ERR_PEER_USER){
                if(DBG) Logger.i(TAG,"disconnected by remote device");
                mBleListener.onDisConnectedByPeer();
                return;
            }
            if (newState == 0){
                resetBLECore();
                return;
            }
            mBleListener.onError(BleConstants.BLE_ERR_UNKNOWN_CONNECTION_STATUS);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            for(BluetoothGattService serviceInList: gatt.getServices()){
                String serviceUUID=serviceInList.getUuid().toString();
                BleServiceStructure serviceType=new BleServiceStructure(serviceInList);
                List <BluetoothGattCharacteristic> characteristics= serviceType.getCharacteristics();
                if(DBG) Logger.i(TAG,"Service discovered "+ serviceUUID);
                for(BluetoothGattCharacteristic characteristicInList : serviceInList.getCharacteristics()){
                    characteristics.add(characteristicInList);
                    if(DBG) Logger.i(TAG,"characteristic found "+ characteristicInList.getUuid());
                }
                mServices.add(serviceType);
            }
            mBleListener.onServiceDiscovered();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(status==0){
                if(DBG) Logger.i(TAG,"characteristic read successfully "+ Arrays.toString(characteristic.getValue()));
                mBleListener.onRead(BleConstants.BLE_SUCCESS,characteristic.getUuid().toString(),characteristic.getValue());
            } else {
                if(DBG) Logger.i(TAG,"characteristic read failed ");
                mBleListener.onRead(BleConstants.BLE_FAILED,characteristic.getUuid().toString(),null);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(status==0){
                if(DBG) Logger.i(TAG,"characteristic write success ");
                mBleListener.onWrite(BleConstants.BLE_SUCCESS,characteristic.getUuid().toString());
            }else{
                if(DBG) Logger.i(TAG,"characteristic write failed ");
                mBleListener.onWrite(BleConstants.BLE_FAILED,characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(DBG) Logger.i(TAG,"characteristic changed "+ Arrays.toString(characteristic.getValue()));
            mBleListener.onDataReceived(characteristic.getValue());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if(status == 0) {
                if (DBG) Logger.i(TAG, "Notification/indication  enabled successfully");
                mBleListener.onDataTransferEnabled();
            }else
                if (DBG) Logger.i(TAG, "Notification/indication  enabled failed");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            mBleListener.onRRSIChanged(rssi);
        }
    }

    // **********************************************************************************************
    // **************************        INTERFACES        ******************************************     onLeScan
    // **********************************************************************************************
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {
        if (DBG) Logger.i(TAG, "Found device" + device.getName()+" with rssi   "+ rssi);
        mBleListener.onScanResults(device, rssi, bytes);
    }

    // **********************************************************************************************
    // **************************    Setters and getters    *****************************************  Setters and getters
    // **********************************************************************************************
    public static int getConnectionStatus(){
        return connectionStatus;
    }

    public  void setDeviceAddress(String address){
        mDeviceAddress = address;
    }

    public String getDeviceAddress(){
        return mDeviceAddress;
    }

    public List<BleServiceStructure> getServices(){
        return mServices;
    }

    public BleServiceStructure getServiceByUUID(String serviceUUID){
        for(BleServiceStructure serviceInList : mServices){
            if(serviceInList.getService().getUuid().toString().equalsIgnoreCase(serviceUUID)){
                return serviceInList;
            }
        }
        return null;
    }

    // **********************************************************************************************
    // **************************          test            ******************************************     bytesToString
    // **********************************************************************************************
    public void toggleDebugLog(){
        if(DBG){
            DBG = false;
        }else{
            DBG = true;
        }
    }


    public static String bytesToString(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder(
                bytes.length);
        for (byte byteChar : bytes)
            stringBuilder.append(String.format("%02X ", byteChar));
        return stringBuilder.toString();
    }

    static JSONObject status = new JSONObject();
    private static void showCoreElementStatus(){
        try {
            if (mBleCore == null) {
                status.put("bleCore", "null");
            }else{
//                status.put("bleCore",mBleCore.toString()+"\n");
                status.put("bleCore","is not null");
            }
            if(mContext == null){
                status.put("mContext","null");
            }else {
//                status.put("mContext",mContext.toString()+"\n");
                status.put("mContext","is not null");

            }
            if(mBluetoothAdapter == null){
                status.put("mBluetoothAdapter","null");
            }else {
//                status.put("mBluetoothAdapter",mBluetoothAdapter.toString()+"\n");
                status.put("mBluetoothAdapter","is not null");

            }
            if(mDevice == null){
                status.put("mDevice","null");
            }else {
//                status.put("mDevice",mDevice.toString()+"\n");
                status.put("mDevice","is not null");

            }
            if(mBluetoothGatt == null){
                status.put("mBluetoothGatt","null");
            }else {
//                status.put("mBluetoothGatt",mBluetoothGatt.toString()+"\n");
                status.put("mBluetoothGatt","is not null");

            }
            if(mGattCallBack == null){
                status.put("mGattCallBack","null");
            }else {
//                status.put("mGattCallBack",mGattCallBack.toString()+"\n");
                status.put("mGattCallBack","is not null");
            }
            if(mServices == null){
                status.put("mServices","null");
            }else {
//                status.put("mGattCallBack",mServices.size()+"service exist");
                status.put("mGattCallBack","is not null");
            }
            Logger.i(TAG, status.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
