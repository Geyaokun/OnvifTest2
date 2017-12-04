package com.punuo.sys.app.onviftest2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by asus on 2017/11/29.
 */

public class MyService extends Service {
    private HttpServer mHttpServer = null;//这个是HttpServer的句柄。
    @Override
    public void onCreate() {
        super.onCreate();
        //在这里开启HTTP Server。
        Log.d("HttpServer", "初始化成功");
        mHttpServer = new HttpServer(8080);
        try {
            mHttpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在这里关闭HTTP Server
        if(mHttpServer != null)
            mHttpServer.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
