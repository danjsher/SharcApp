package com.danjsher.sharcapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private DatagramSocket armSocket    = null;
    private DatagramSocket sleeveSocket = null;
    private String clientState = Constants.STOPPED;
    private String serverState = Constants.STOPPED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create socket for communicating with sharc arm
        try {
            if(armSocket == null) {
                armSocket = new DatagramSocket(12346);
                armSocket.setReuseAddress(true);
                // set ten second time out on socket
                armSocket.setSoTimeout(10000);
            }

            if ( sleeveSocket == null ) {
                sleeveSocket = new DatagramSocket(12347);
                sleeveSocket.setReuseAddress(true);
                // set ten second time out on socket
                sleeveSocket.setSoTimeout(10000);
            }
        } catch (Exception e) {
        }
        /*
        // Creating a new intent to start the polling service
        Intent mServiceIntent = new Intent(this, StatPollService.class);
        this.startService(mServiceIntent);

        // create intent filter for StatPollService broadcasts
        IntentFilter mStatusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);

        // create the broadcast receiver
        ResponseReceiver mResponseReceiver = new ResponseReceiver();

        // register broadcast receiver with intent filter
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mResponseReceiver,
                mStatusIntentFilter
        );
        */

        // Button click handlers

        final Button armStartButton = (Button)findViewById(R.id.ArmStartButton);

        armStartButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new SharcComThread().execute(
                                new ComParams("1",
                                        armSocket,
                                        "192.168.23.19",
                                        12346,
                                        (TextView)findViewById(R.id.ArmStatusText)
                                )
                        );
                    }
                }
        );

        final Button armStopButton = (Button)findViewById(R.id.ArmStopButton);

        armStopButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new SharcComThread().execute(
                                new ComParams("0",
                                        armSocket,
                                        "192.168.23.19",
                                        12346,
                                        (TextView)findViewById(R.id.ArmStatusText)
                                )
                        );
                    }
                }
        );

        final Button sleeveLiveModeButton = (Button)findViewById(R.id.SleeveLiveModeButton);

        sleeveLiveModeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new SharcComThread().execute(
                                new ComParams("1",
                                        sleeveSocket,
                                        "192.168.23.1",
                                        12347,
                                        (TextView)findViewById(R.id.SleeveStatusText)
                                )
                        );
                    }
                }
        );

        final Button sleeveStopButton = (Button)findViewById(R.id.SleeveStopButton);

        sleeveStopButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new SharcComThread().execute(
                                new ComParams("0",
                                        sleeveSocket,
                                        "192.168.23.1",
                                        12347,
                                        (TextView)findViewById(R.id.SleeveStatusText)
                                )
                        );
                    }
                }
        );

        final Button sleeveRecordModeButton = (Button)findViewById(R.id.SleeveRecordModeButton);

        // tag for switching between play and record
        sleeveRecordModeButton.setTag(0);

        sleeveRecordModeButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        final int status = (Integer) v.getTag();

                        ComParams comParams = new ComParams("3",
                                sleeveSocket,
                                "192.168.23.1",
                                12347,
                                (TextView)findViewById(R.id.SleeveStatusText)
                        );

                        if(status == 0) {
                            // get record time
                            EditText recordTimeInput = (EditText)findViewById(R.id.RecordTimeInputText);
                            TextView sleeveStatusText = (TextView)findViewById(R.id.SleeveStatusText);
                            sleeveStatusText.setText(recordTimeInput.getText().toString());

                            // set message code
                            comParams.message = "2";

                            // send record message
                            new SharcComThread().execute(comParams);

                            // change button to play
                            sleeveRecordModeButton.setText("Play");
                            v.setTag(1);
                        } else if(status == 1) {
                            // set message code
                            comParams.message = "3";

                            // send play message code
                            new SharcComThread().execute(comParams);
                            // change button back to record
                            sleeveRecordModeButton.setText("Record");
                            v.setTag(0);
                        }
                    }
                }
        );

        final Button calibrateButton = (Button)findViewById(R.id.CalibrateButton);

        calibrateButton.setTag(0);

        calibrateButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        final int status = (Integer) v.getTag();

                        ComParams comParams = new ComParams("4",
                                sleeveSocket,
                                "192.168.23.1",
                                12347,
                                (TextView)findViewById(R.id.CalibrateButtonStatusText)
                        );



                        switch(status) {
                            case 0:
                                comParams.message = "4";
                                // put arm in calibrate mode
                                ComParams armComParams = new ComParams( "4",
                                        armSocket,
                                        "192.168.23.19",
                                        12346,
                                        (TextView)findViewById(R.id.CalibrateButtonStatusText)
                                );

                                new SharcComThread().execute(armComParams, comParams);
                                calibrateButton.setText("Start Bicep Flex");
                                v.setTag(1);
                                break;
                            case 1:
                                comParams.message = "5";
                                new SharcComThread().execute(comParams);
                                calibrateButton.setText("Start Shoulder Flex");
                                v.setTag(2);
                                break;
                            case 2:
                                comParams.message = "6";
                                new SharcComThread().execute(comParams);
                                calibrateButton.setText("Start Shoulder Rot Rest");
                                v.setTag(3);
                                break;
                            case 3:
                                comParams.message = "7";
                                new SharcComThread().execute(comParams);
                                calibrateButton.setText("Start Shoulder Rot F/B");
                                v.setTag(4);
                                break;
                            case 4:
                                comParams.message = "8";
                                new SharcComThread().execute(comParams);
                                calibrateButton.setText("Complete Calibration");
                                v.setTag(5);
                                break;
                            case 5:
                                comParams.message = "0"; // put sleeve in stopped state
                                new SharcComThread().execute(comParams);
                                calibrateButton.setText("Calibrate");
                                v.setTag(0);
                                break;
                            default:
                                break;
                        }
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SharcComThread extends AsyncTask<ComParams, Void, ComParams> {

        private static final String TAG = "sharcLog";

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

        protected void onPostExecute(ComParams result){
            TextView messageContent = result.statusTextView;
            messageContent.setText(result.response);
            return;
        }
    }

    public class ComParams {
        public DatagramSocket udpSocket; //socket to send message on
        public String message;           // message code to send
        public String ipAddr;            // ip address to send to
        public int port;                 // destination port
        public String response;          // response from server
        public TextView statusTextView;  // text view to update

        public ComParams(String m, DatagramSocket s, String ip, int p, TextView v) {
            udpSocket = s;
            message   = m;
            ipAddr    = ip;
            port      = p;
            response  = "";
            statusTextView = v;
        }
    }
}

