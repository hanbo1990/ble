package com.mgtech.lib.blelib;

/**
 * Created by hanbo on 2016/3/7 0007.
 */
public class BleConstants {

    /** auto pair passwd */
    public static final byte[] PAIR_PASSWD = {49, 50, 51, 52, 53, 54};

    public static final String PASSWD = "000000";

    /**                                CHARACTERISTIC PERMISSION
     *  Characteristic permission for atmel c1000, this is quite different from android definition
     *  however, when reading permission, according to chip definition
     */
    public static final int AT_BLE_PERMISSION_CHAR_BROADCST = 0x01;

    /** According to atmel c1000 definition, different from bluedroid, Remote characteristic read permission  */
    public static final int AT_BLE_PERMISSION_CHAR_READ = 0x02;

    /** According to atmel c1000 definition,different from bluedroid, Remote characteristic write without response  */
    public static final int AT_BLE_PERMISSION_CHAR_WRITE_WITHOUT_RESPONSE = 0x04;

    /** According to atmel c1000 definition,different from bluedroid, Remote characteristic write permission  */
    public static final int AT_BLE_PERMISSION_CHAR_WRITE = 0x08;

    /** According to atmel c1000 definition,different from bluedroid, Remote characteristic signed write  */
    public static final int AT_BLE_PERMISSION_CHAR_SIGNED_WRITE =0x40;

    /** According to atmel c1000 definition,different from bluedroid, Remote characteristic reliable write  */
    public static final int AT_BLE_PERMISSION_CHAR_RELIABLE_WRITE = 0x80;




    /**                                     ERROR CODE:
     *  existing gatt error occured but not in the java file, found in C file of google
     * Unrecoveralbe error: normally caused by maxium GATT client, we have resert the number every time
     */
    /** Fatal GATT error 133  */
    public final static int GATT_ERROR = 133;

    /** Fatal GATT error 133  */
    public final static int GAP_EVT_CONN_CLOSED = 257;
    /** Error Connection timeout  */
    public final static int HCI_ERR_CONNECTION_TOUT = 8;
    /** remote user error  */
    public final static int HCI_ERR_PEER_USER = 19;
    // 62


    /**                     Connection Status     */
    /**   STATE DISCONNECTED     */
    public final static int STATE_DISCONNECTED = 0;
    /**   STATE CONNECTING     */
    public final static int STATE_CONNECTING = 1;
    /**   STATE CONNECTED     */
    public final static int STATE_CONNECTED = 2;
    /**   STATE DISCONNECTING     */
    public final static int STATE_DISCONNECTING = 3;


    /** Bluetooth config descriptor     */
    public final static String CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    /**
     *          BLUETOOTH OPERATION RESULT
     */
    public static final boolean BLE_SUCCESS = true;
    public static final boolean BLE_FAILED = false;


    /**
     *          BLECORE ERROR CODE
     */

    /**  no bluetooth adapter error    */
    public static final int BLE_ERR_NULL_ADAPTER = 2;

    /**  scan status abnormal    */
    public static final int BLE_ERR_SCAN_ABNORMAL = 3;

    /** Pair device */
//    public static final int BLE_ERR_PAIR_WRONG_DEVICE = 1;

    /** FATAL ERROR, device need reset */
    public static final int BLE_ERR_FATAL = 5;

    /** Unknown connection status error     */
    public static final int BLE_ERR_UNKNOWN_CONNECTION_STATUS = 4;

    /** Unsuitable operation called, for example call gatt function when there is no connection */
    public static final int BLE_ERR_WRONG_OPERATION = 6;

    /** characteristic has no read permission     */
    public static final int BLE_ERR_NO_READ_PERMISSION = 7;

    /** characteristic does not exist     */
    public static final int BLE_ERR_CHARACTERISTIC_NOT_FOUND = 8;

    /** characteristic has no write permission     */
    public static final int BLE_ERR_NO_WRITE_PERMISSION = 10;

    /** characteristic has no notify property     */
    public static final int BLE_ERR_NO_NOTIFY_PERMISSION = 11;

    /** characteristic has no indicate property     */
    public static final int BLE_ERR_NO_INDICATION_PERMISSION = 9;

}
