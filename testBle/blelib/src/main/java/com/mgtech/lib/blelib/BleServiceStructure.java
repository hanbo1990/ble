package com.mgtech.lib.blelib;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanbo on 2016/6/29 0029.
 */
public class BleServiceStructure {
    private BluetoothGattService mService;
    private List<BluetoothGattCharacteristic> mCharacteristics;

    BleServiceStructure(BluetoothGattService service) {
            mService = service;
            mCharacteristics= new ArrayList<>();
        }

        public BluetoothGattService getService() {return mService;}
        public List<BluetoothGattCharacteristic> getCharacteristics () {return mCharacteristics;}
}
