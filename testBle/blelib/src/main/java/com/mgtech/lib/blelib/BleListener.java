package com.mgtech.lib.blelib;

import android.bluetooth.BluetoothDevice;

/**
 * Created by hanbo on 2016/6/21 0021.
 */
public interface BleListener {

    /**
     * Scanner result interface
     *@param device  the scanned device
     *@param rssi rssi of the device
     *@param bytes broadcast Package
     */
    void onScanResults(BluetoothDevice device, int rssi, byte[] bytes);

    /**
     * Connnection made callback
     */
    void onConnected(String address);

    /**
     * Connection Disconnected callback
     */
    void onDisConnected();

    /**
     * Connection Disconnected by peer callback
     */
    void onDisConnectedByPeer();

    /**
     * Connection timeout callback
     */
    void onTimeOut();

    /**
     * Service discovered callback
     */
    void onServiceDiscovered();

    /**
     * Write characteristic success callback
     * @param  status status of the operation, true if success, false if failed
     * @param s UUID of the written characteristic
     */
    void onWrite(boolean status,String s);

    /**
     * Read characteristic success callback
     * @param  status status of the operation, true if success, false if failed
     * @param s      UUID of the characteristic
     * @param value  Value of the characteristic
     */
    void onRead(boolean status, String s, byte[] value);

    /**
     * Enable data transfer via indication or notification success
     */
    void onDataTransferEnabled();

    /**
     * Receive data from indication or notification characteristic
     * @param receivedData
     */
    void onDataReceived(byte[] receivedData);

    /**
     * RSSI change callback
     * @param rssi the changed rssi
     */
    void onRRSIChanged(int rssi);

    /**
     * If there is an Error
     * @param Error error code
     */
    void onError(int Error);

    /**
     * If pair status of Ble Changed
     */
    void onPairStatusChanged(boolean pairStatus);

    /**
     * If received pair request
     */
    void onPairRequest(int PairType);

}
