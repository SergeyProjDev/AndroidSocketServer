package com.novelist.ipflashlight;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = findViewById(R.id.address);
        editTextPort =    findViewById(R.id.port);
        buttonLight =   findViewById(R.id.light);
        textResponse =    findViewById(R.id.response);

        buttonLight.setOnClickListener(btnLightListener);
    }

    View.OnClickListener btnLightListener =
            new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    ClientTask client = new ClientTask(
                            editTextAddress.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()));
                    client.execute();
                }};

    public class ClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        ClientTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
        }

        // connecting and getting response
        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

                while ((bytesRead = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }finally{
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException ignored) { }
                }
            }
            return null;
        }

        // printing response
        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }

}