package com.danjsher.sharcapp;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by dan on 4/6/2016.
 */
public class CalibrationAsyncTask extends AsyncTask<CalibrationParameters, Void, CalibrationParameters> {

    private static final String TAG = "sharcLog";

    @Override
    protected CalibrationParameters doInBackground(CalibrationParameters... params) {
        for(CalibrationParameters p : params) {
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
        if(isCancelled()){
            Log.i(TAG, "cancelled");
        }
        if(params.length == 2) {
            // initial message has two messages to send, the second one is what we
            // want to keep using with the continuous thread
            return params[1];
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(final CalibrationParameters result) {
        int waitTime = 250;

        if(result.response.equals("STOPPED")) {
            Log.i(TAG, "Stopping task");
        } else {
            String[] numbers = parseResponse(result.response, Integer.parseInt(result.message));
            int i = 0;
            for(TextView v: result.statusTextView) {
                v.setText(numbers[i++]);
            }
            Log.i(TAG, "Running calib task again");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    new CalibrationAsyncTask().execute(result);
                }
            };

            Handler h = new Handler();
            h.postDelayed(r, waitTime);
        }
    }

    private String[] parseResponse(String response, int offset){
        String[] split = response.split(" ");

        String[] ret = new String[2];
        if(offset == 5) {
            ret[0] = split[0];
            ret[1] = split[1];
        } else if (offset == 6) {
            ret[0] = split[2];
            ret[1] = split[3];
        } else if (offset == 7) {
            ret[0] = split[4];
            ret[1] = split[5];
        }
        return ret;
    }
}
