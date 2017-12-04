package com.punuo.sys.app.onviftest2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DatagramPacket mPacket;
    private boolean mIsSearching=true;
    private static final Integer broadcast_port = 3702;
    private static final String broadcast_ip = "239.255.255.250";
    private MulticastSocket mSocket;
    private InetAddress address;
    private String ip;
    private String ipv4;
    private String ipv6;
    private Http8000 http8000;
    WifiManager.MulticastLock multicastLock;
    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent startIntent=new Intent(MainActivity.this,MyService.class);
        startService(startIntent);
//        http8000 =new Http8000();
        allowMulticast();
        ipv6=getLocalHostIp().substring(0,25);
        try {
            ipv4=getLocalIPAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d(TAG, ipv6);
        new Thread(mSearchingRunnable).start();
    }

    public String getLocalHostIp()
    {
        String ipaddress = "";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements())
            {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    // 在这里如果不加isIPv4Address的判断,直接返回,在4.0上获取到的是类似于fe80::1826:66ff:fe23:48e%p2p0的ipv6的地址
                    return ipaddress =ip.getHostAddress();
                }

            }
        }
        catch (SocketException e)
        {
            Log.e(TAG, "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;

    }
    private String getLocalIPAddress() throws SocketException{
        for(Enumeration en = NetworkInterface.getNetworkInterfaces();en.hasMoreElements();){
            NetworkInterface intf = (NetworkInterface) en.nextElement();
            for(Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();){
                InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                if(!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)){
                    return inetAddress.getHostAddress().toString();
                }
            }
        }
        return "null";
    }

    private void allowMulticast(){
        @SuppressLint("WifiManagerLeak")
        WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        multicastLock=wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
    }
    private Runnable mSearchingRunnable=new Runnable() {
        @Override
        public void run() {
            initSocket();
            byte[] Buff = new byte[4096];
            DatagramPacket packet = new DatagramPacket(Buff, Buff.length);
            while(mIsSearching) {
                try {
                    mSocket.receive(packet);
                    ip=packet.getAddress().toString().substring(1,packet.getAddress().toString().length());
                    Log.d(TAG, ip);
                    if(packet.getLength() > 0) {
                        String e = new String(packet.getData(), 0, packet.getLength());
                        Log.v(TAG, " receive packets:" + e);
                        processReceivedPacket(e);
                    }
                } catch (InterruptedIOException var4) {
                    Log.d(TAG, "receive timeout!!");
                } catch (IOException var5) {
                    var5.printStackTrace();
                    break;
                }
            }
            mSocket.close();
        }
    };

    private void initSocket() {
        try {
            mSocket=new MulticastSocket(broadcast_port);
            address=InetAddress.getByName(broadcast_ip);
            mSocket.joinGroup(address);
            mSocket.setSoTimeout(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMid(String src, String head, String foot) {
        int headIndex = src.indexOf(head);
        if(headIndex == -1) {
            return null;
        } else {
            String tmp = src.substring(headIndex + head.length());
            int footIndex = tmp.indexOf(foot);
            return footIndex == -1?null:tmp.substring(0, footIndex);
        }
    }

    private void processReceivedPacket(String packet) {
        Log.d(TAG, packet);
        Log.d(TAG, "Packet: "+packet.indexOf("uuid:"));
        Log.d(TAG, "Packet: "+packet.indexOf("</wsa"));
        if ((packet.indexOf("uuid:")<0)|(packet.indexOf("</wsa")<0)){
            return;
        }
        if ((packet.indexOf("uuid:"))>(packet.indexOf("</wsa"))){
            return;
        }
        uuid=packet.substring(packet.indexOf("uuid:")+5,packet.indexOf("</wsa"));
        Log.d(TAG, uuid);
        sendResponse();
    }
    private void sendResponse() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buf =String.format(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:SOAP-ENC=\"http://www.w3.org/2003/05/soap-encoding\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" xmlns:wsdd=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\" xmlns:chan=\"http://schemas.microsoft.com/ws/2005/02/duplex\" xmlns:wsa5=\"http://www.w3.org/2005/08/addressing\" xmlns:xmime=\"http://tempuri.org/xmime.xsd\" xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" xmlns:wsrfbf=\"http://docs.oasis-open.org/wsrf/bf-2\" xmlns:wstop=\"http://docs.oasis-open.org/wsn/t-1\" xmlns:tt=\"http://www.onvif.org/ver10/schema\" xmlns:ns3=\"http://www.onvif.org/ver10/pacs\" xmlns:wsrfr=\"http://docs.oasis-open.org/wsrf/r-2\" xmlns:ns1=\"http://www.onvif.org/ver10/actionengine/wsdl\" xmlns:ns2=\"http://www.onvif.org/ver10/accesscontrol/wsdl\" xmlns:ns4=\"http://www.onvif.org/ver10/doorcontrol/wsdl\" xmlns:ns5=\"http://www.onvif.org/ver10/advancedsecurity/wsdl\" xmlns:tad=\"http://www.onvif.org/ver10/analyticsdevice/wsdl\" xmlns:tan=\"http://www.onvif.org/ver20/analytics/wsdl\" xmlns:tdn=\"http://www.onvif.org/ver10/network/wsdl\" xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" xmlns:tev=\"http://www.onvif.org/ver10/events/wsdl\" xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\" xmlns:timg=\"http://www.onvif.org/ver20/imaging/wsdl\" xmlns:tls=\"http://www.onvif.org/ver10/display/wsdl\" xmlns:tmd=\"http://www.onvif.org/ver10/deviceIO/wsdl\" xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\" xmlns:trc=\"http://www.onvif.org/ver10/recording/wsdl\" xmlns:trp=\"http://www.onvif.org/ver10/replay/wsdl\" xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" xmlns:trv=\"http://www.onvif.org/ver10/receiver/wsdl\" xmlns:tse=\"http://www.onvif.org/ver10/search/wsdl\"><SOAP-ENV:Header><wsa:MessageID>uuid:3419d68a-2dd2-21b2-a205-002324401C7B</wsa:MessageID><wsa:RelatesTo>uuid:%s</wsa:RelatesTo><wsa:To SOAP-ENV:mustUnderstand=\"true\">http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:To><wsa:Action SOAP-ENV:mustUnderstand=\"true\">http://schemas.xmlsoap.org/ws/2005/04/discovery/ProbeMatches</wsa:Action></SOAP-ENV:Header><SOAP-ENV:Body><wsdd:ProbeMatches><wsdd:ProbeMatch><wsa:EndpointReference><wsa:Address>urn:uuid:3419d68a-2dd2-21b2-a205-002324401C7B</wsa:Address><wsa:ReferenceProperties></wsa:ReferenceProperties><wsa:ReferenceParameters></wsa:ReferenceParameters><wsa:PortType>ttl</wsa:PortType></wsa:EndpointReference><wsdd:Types>tds:Device</wsdd:Types><wsdd:Scopes>onvif://www.onvif.org/type/NetworkVideoTransmitter</wsdd:Scopes><wsdd:XAddrs>http://%s:8080/onvif/device_service</wsdd:XAddrs><wsdd:MetadataVersion>1</wsdd:MetadataVersion></wsdd:ProbeMatch></wsdd:ProbeMatches></SOAP-ENV:Body></SOAP-ENV:Envelope>"),new Object[]{uuid,ipv4}).getBytes();
                try {
                    mPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(ip), 3702);
                    mSocket.send(mPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "send response!");

            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,MyService.class));
    }
}
