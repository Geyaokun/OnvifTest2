package com.punuo.sys.app.onviftest2;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by asus on 2017/11/24.
 */

public class Http8000 {
    private static final String TAG = "Http8000";
    private ServerSocket mSocket;
    private ExecutorService executorService= Executors.newSingleThreadExecutor();
    private static final Integer port=8000;

    public Http8000(){
        initSocket();
        initReceiverMessage();
    }

    private void initSocket() {
        try {
            mSocket=new ServerSocket(port);
            Log.d("HttpServer", "initSocket: "+String.valueOf(mSocket!=null));
            Log.d("HttpServer", "isBound "+mSocket.isBound()+" isClosed "+mSocket.isClosed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initReceiverMessage() {
        executorService.execute(runnableReceiveMsg);
    }

    private Runnable runnableReceiveMsg=new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = mSocket.accept();
                    InputStream inputStream = socket.getInputStream();
                    byte buffer[] = new byte[10 * 1024];
                    int temp = 0;
                    while ((temp = inputStream.read(buffer)) != -1) {
                        Log.d(TAG, new String(buffer, 0, temp));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
