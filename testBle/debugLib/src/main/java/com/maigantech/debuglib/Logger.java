package com.maigantech.debuglib;

import android.util.Log;

/**
 * Created by hanbo1990 on 2016/1/25 0025.
 */
public class Logger {

    public static boolean isTrue = true;

    public static int LOG_LEVEL = 4;
    public static int ERROR = 1;
    public static int WARN = 2;
    public static int INFO = 3;
    public static int DEBUG = 4;
    public static int VERBOS = 5;


    public static void e(String tag,String msg){
        if(LOG_LEVEL>ERROR && isTrue)
            Log.e("MGXYZ " + tag, msg);
    }

    public static void w(String tag,String msg){
        if(LOG_LEVEL>WARN && isTrue)
            Log.w("MGXYZ " + tag, msg);
    }
    public static void i(String tag,String msg){
        if(LOG_LEVEL>INFO && isTrue)
            Log.i("MGXYZ " + tag, msg);
    }
    public static void d(String tag,String msg){
        if(LOG_LEVEL>DEBUG && isTrue)
            Log.d("MGXYZ " + tag, msg);
    }
    public static void v(String tag,String msg){
        if(LOG_LEVEL>VERBOS && isTrue)
            Log.v("MGXYZ " + tag, msg);
    }
}



