package com.example.hanbo.myapplication;

/**
 * Created by hanbo on 2016/6/30 0030.
 */
public class RemoteBluetoothConfig {


    // service and parameters in it
    public static final String MG_PROFILE_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String BT_CHAR_DATA = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static final String BT_CHAR_CONF = "0000fff2-0000-1000-8000-00805f9b34fb";

    public static final int STATE_WRITE_FOR_CP_DATA = 16;
    //TODO
    public static final int STATE_WRITE_FOR_CP_ECG_DATA = 16;
    public static final int STATE_WRITE_FOR_START_SAMPLE = 20;
    public static final int STATE_WRITE_FOR_DATA = 21;

    public static final int STATE_UPDATE_SOFTWARE = 40;
    public static final int STATE_UPDATE_SOFTWARE_CONTENT = 41;

}
