package nittcprocon.vrplayer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by tukitan on 17/01/13.
 */

public class SocketTCP {
    private DatagramSocket receiveSocket;
    private String message,hoge;
    private int port;
    private Socket mSocket;
    private BufferedReader in;

    private ServerSocket mServerSocket;

    //別スレッドで実行がほぼ必須
    public String getMessage(int ports){

        port = ports;

        Thread thread=new Thread(){
            public void run() {
                // portを監視するTCPソケットを生成
                // DatagramSocket receiveSocket = null;
                try {
                    mServerSocket = new ServerSocket(port);
                    mSocket = mServerSocket.accept();
                    in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 受信したデータをログへ出力
                try {
                    if(in!=null) message = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("TCP", "Message :  " + message);
            }
        };
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.d("TCP","Message :  " + e.toString());
        }

        return message;
    }
}
