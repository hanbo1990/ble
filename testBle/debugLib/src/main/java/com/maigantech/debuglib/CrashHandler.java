package com.maigantech.debuglib;

import android.content.Context;
import android.os.Looper;

/**
 * Created by hanbo1990 on 2016/1/26 0026.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler{

    private static final String TAG = "CrashHandler";

    private static CrashHandler crashHandler;

    private Context mContext;

    private Thread.UncaughtExceptionHandler mUnCaughtExceptionHandler;

    private CrashHandler(){}

    public static CrashHandler getInstance(){
        if (crashHandler == null)
            crashHandler = new CrashHandler();
        return  crashHandler;
    }

    public void init(Context mContext){
        this.mContext = mContext;
        mUnCaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable throwable) {
        Logger.e(TAG,"uncaughtException");
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                throwable.printStackTrace();
                Toaster.showToast(mContext.getApplicationContext(), "程序奔溃", 1000);
                Looper.loop();

            }
        }.start();
    }
}
