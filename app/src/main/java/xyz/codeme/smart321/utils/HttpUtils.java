package xyz.codeme.smart321.utils;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HttpUtils {
    public final static String TAG = "smart321http";
    public final static int SEND_SUCCESS  = 0xac01;
    public final static int SEND_TIME_OUT = 0xac02;
    public final static int SEND_FAILED   = 0xac03;

    public final static String ORDER_OPEN_CLOSE = "opendoor";
    public final static String ORDER_OPEN_DOOR = "justopen";
    public final static String ORDER_CLOSE_DOOR = "closedoor";
    public final static String ORDER_ADJUST_DOOR = "adjustdoor";
    public final static String ORDER_EXIT_PI = "exitpi";

    private static String localIP = "192.168.5.105";
    private static int localPort  = 8088;
    private static String romateIP = "45.62.118.214";
    private static int romatePort  = 8088;

    private String identify;
    private Handler handler;

    public HttpUtils(Handler handler) {
        this.handler = handler;
    }

    public void sendLocalOpenOrder() {
        sendOrder(localIP, localPort, ORDER_OPEN_CLOSE);
    }

    public void sendRomateOpenOrder() {
        sendOrder(romateIP, romatePort, ORDER_OPEN_CLOSE);
    }

    public void sendLocalOrder(String order) {
        sendOrder(localIP, localPort, order);
    }

    public void playMusic(String fileName) {
        sendOrder(localIP, localPort, "sound[" + fileName + "]");
    }

    private void sendOrder(String ip, int port, String order) {
        Thread sendThread = new Thread(new SocketLink(ip,port,order));
        sendThread.start();
    }

    private class SocketLink implements Runnable {
        private String ip;
        private int port;
        private String order;

        public SocketLink(String ip, int port, String order) {
            this.ip = ip;
            this.port = port;
            this.order = order.trim();
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(ip, port);
                socket.setSoTimeout(3000);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                // 接收消息
                char[] recv = new char[20];
                reader.read(recv);
                if(! new String(recv).trim().equals("success")) {
                    socket.close();
                    handler.sendEmptyMessage(SEND_FAILED);
                    return;
                }

                // 发送身份
                writer.println(String.format("[%s]", identify));
                writer.flush();

                // 发送指令
                writer.println(order);
                writer.flush();

                socket.close();
                Log.i(TAG, "Order success: " + order);
                handler.sendEmptyMessage(SEND_SUCCESS);
            } catch (SocketTimeoutException e) {
                Log.w(TAG, "socket timeout");
                handler.sendEmptyMessage(SEND_TIME_OUT);
            } catch (IOException e) {
                Log.w(TAG, "socket io error");
                handler.sendEmptyMessage(SEND_FAILED);
            }
        }
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }
}
