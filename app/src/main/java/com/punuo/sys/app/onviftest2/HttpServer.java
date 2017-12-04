package com.punuo.sys.app.onviftest2;

import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.IStatus;
import org.nanohttpd.protocols.http.response.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

/**
 * Created by asus on 2017/11/29.
 */

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    public enum Status implements IStatus {
        NOT_USE_POST(700, "not use post");

        private final int requestStatus;
        private final String description;

        Status(int requestStatus, String description) {
            this.requestStatus = requestStatus;
            this.description = description;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public int getRequestStatus() {
            return 0;
        }
    }

    public HttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG, "serve: "+"收到消息");
        Log.d(TAG, "serve: "+session.getUri());
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        Map<String, String> parms=session.getParms();
        if (parms!=null){
        Log.d(TAG, "serve: "+session.getMethod()+parms.toString());}
        /*我在这里做了一个限制，只接受POST请求。这个是项目需求。*/
        if (Method.POST.equals(session.getMethod())) {
            Map<String, String> files = new HashMap<String, String>();
            /*获取header信息，NanoHttp的header不仅仅是HTTP的header，还包括其他信息。*/
            Map<String, String> header = session.getHeaders();

            try {
                /*这句尤为重要就是将将body的数据写入files中，大家可以看看parseBody具体实现，倒现在我也不明白为啥这样写。*/
                session.parseBody(files);
                /*看就是这里，POST请教的body数据可以完整读出*/
                String body = session.getQueryParameterString();
                Log.d(TAG, "header : " + header);
                Log.d(TAG, "body : " + body);
                /*这里是从header里面获取客户端的IP地址。NanoHttpd的header包含的东西不止是HTTP heaer的内容*/
                header.get("http-client-ip");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            /*这里就是为客户端返回的信息了。我这里返回了一个200和一个HelloWorld*/
            return newFixedLengthResponse(Status.NOT_USE_POST, "text/html", "HelloWorld");
        }else
            return newFixedLengthResponse(Status.NOT_USE_POST, "text/html", "use post");
    }

}
