package com.danjsher.sharcapp;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class TemperatureAsyncTask extends AsyncTask<CalibrationParameters, Void, CalibrationParameters> {

    private static final String TAG = "sharcLog";

    @Override
    protected CalibrationParameters doInBackground(CalibrationParameters... params) {
        for (CalibrationParameters p : params) {
            int serverPort = p.port;
            String response = "No message";
            DatagramPacket sendMessagePacket = null;
            DatagramPacket recvMessagePacket = null;
            //DatagramSocket udpSocket = null;

            String messageStr = p.message;
            String recvText;

            InetAddress serverAddress;

            int messageLength = messageStr.length();
            byte[] message = messageStr.getBytes();
            Log.i(TAG, "MAKE SURE YOU'RE ON SHARC WiFi");
            try {
                Log.i(TAG, "getting address name of " + p.ipAddr);
                serverAddress = InetAddress.getByName(p.ipAddr);
                sendMessagePacket = new DatagramPacket(message, messageLength, serverAddress, serverPort);

            } catch (UnknownHostException e) {
                Log.i(TAG, "failed to get host name");
            }


            byte[] recvBuffer = new byte[1500];
            recvMessagePacket = new DatagramPacket(recvBuffer, recvBuffer.length);

            try {
                Log.i(TAG, "sending data");
                p.udpSocket.send(sendMessagePacket);
                Log.i(TAG, "receiving data");
                p.udpSocket.receive(recvMessagePacket);
                recvText = new String(recvBuffer, 0, recvMessagePacket.getLength());
                Log.i(TAG, "received: " + recvText);
                p.response = recvText;
            } catch (IOException e) {
                Log.i(TAG, "Message timed out or message send error");
                p.response = "Error: Message Timeout";

            }
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(final CalibrationParameters result) {
        int waitTime = 250;
        if(result.response.equals("Error: Message Timeout")) {
            Log.i(TAG, "Message Time out, exiting thread");
        } else {
            // only keep one digit
            DecimalFormat df = new DecimalFormat("#.#");
            // loop through each result and assign it to textview
            int i = 0;
            String split[] = result.response.split(" ");
            for (TextView v : result.statusTextView) {
                v.setText(df.format(Float.parseFloat(split[i++])));
            }
        }
        Log.i(TAG, "Running temp task again");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                new TemperatureAsyncTask().execute(result);
            }
        };

        Handler h = new Handler();
        h.postDelayed(r, waitTime);
    }
}
