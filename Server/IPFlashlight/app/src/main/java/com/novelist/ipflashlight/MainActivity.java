package com.novelist.ipflashlight;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    boolean light = false;
    String message = "";

    ServerSocket serverSocket;
    Camera camera;

    // UI components
    TextView info, infoip, msg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info =   findViewById(R.id.info);
        infoip = findViewById(R.id.infoip);
        msg =    findViewById(R.id.msg);

        infoip.setText(getIpAddress());

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> NetIfaces = NetworkInterface.getNetworkInterfaces();
            while (NetIfaces.hasMoreElements()) {
                NetworkInterface networkI = NetIfaces.nextElement();
                Enumeration<InetAddress> InetAddress = networkI.getInetAddresses();
                while (InetAddress.hasMoreElements()) {
                    InetAddress inetAddress = InetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Local Server Address: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            ip += "Error! " + e.toString() + "\n";
        }
        return ip;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) { }
        }
    }



    private class SocketServerThread extends Thread {

        @Override
        public void run() {

            try {

                // set port
                serverSocket = new ServerSocket(8080);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("Port: " + serverSocket.getLocalPort());
                    }
                });

                // client listener
                while (true) {
                    Socket socket = serverSocket.accept();
                    message += "From " + socket.getInetAddress().getHostAddress() + " : ";

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // from SocketServerReplayThread light info
                            msg.setText(message);
                        }
                    });
                    new SocketServerReplyThread(socket).run();
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }



    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;

        SocketServerReplyThread(Socket socket) {
            hostThreadSocket = socket;
        }

        @Override
        public void run() {

            String msgReply;
            light = !light; // change light state

            if (light) { // light ON
                camera = Camera.open();
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);
                camera.startPreview();
                msgReply = "Light ON";
            }
            else{ // light OFF
                camera.stopPreview();
                camera.release();
                camera = null;
                msgReply = "Light OFF";
            }

            try { // replay
                PrintStream printS = new PrintStream(hostThreadSocket.getOutputStream());
                printS.print(msgReply);
                printS.close();

                message += msgReply + "\n";

            } catch (Exception e) {
                message += "Error! " + e.toString() + "\n";
            }

            // print message
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    msg.setText(message);
                }
            });
        }

    }
}