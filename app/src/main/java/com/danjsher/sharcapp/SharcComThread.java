package com.danjsher.sharcapp;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SharcComThread extends AsyncTask<ComParams, Void, ComParams> {

    private static final String TAG = "sharcLog";
    private Button buttonPressed = null;
    private static RecordActivity mRecActivity;

    public SharcComThread(Button bp) {
        buttonPressed = bp;
    }

    public SharcComThread(RecordActivity ra) {
        mRecActivity = ra;
    }

    public SharcComThread(){
        super();
    }
    @Override
    protected void onPreExecute() {
        // if a button was provided, disable it until work is done
        if(buttonPressed != null) {
            buttonPressed.setEnabled(false);
        }
    }

    @Override
    protected ComParams doInBackground(ComParams... params){
        for(ComParams p : params) {
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
                Log.i(TAG, "sending data " + p.message);
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

    protected void onPostExecute(ComParams result){
        if(result.statusTextView != null && !result.message.equals("2")) {
            TextView messageContent = (TextView)result.statusTextView;
            messageContent.setText(result.response);
        } else if ( result.message.equals("11")){
            Log.i(TAG, "Setting up list");
            String[] fileNames = result.response.split(" ");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mRecActivity, R.layout.list_items, fileNames);
            result.mListView.setAdapter(adapter);
        }

        // enable button once message has been received
        if(buttonPressed != null) {
            buttonPressed.setEnabled(true);
        }
        return;
    }
}