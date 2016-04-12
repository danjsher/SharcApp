package com.danjsher.sharcapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.DatagramSocket;

public class TemperatureActivity extends AppCompatActivity {
    private static DatagramSocket armSocket = null;
    private TemperatureAsyncTask mTempTask = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        // create socket for communicating with sharc arm
        try {
            if(armSocket == null) {
                armSocket = new DatagramSocket(12348);
                armSocket.setReuseAddress(true);
                // set ten second time out on socket
                armSocket.setSoTimeout(5000);
            }
        } catch (Exception e) {

        }
        //spawn polling for temp
        if(mTempTask == null) {
            mTempTask = new TemperatureAsyncTask();
            mTempTask.execute(new CalibrationParameters(
                    "3",
                    armSocket,
                    "192.168.23.19",
                    12346,
                    (TextView) findViewById(R.id.shoulderRotationServoTempText),
                    (TextView) findViewById(R.id.shoulderFlexServoTempText),
                    (TextView) findViewById(R.id.bicepFlexServoTempText),
                    (TextView) findViewById(R.id.bicepRotationServoTempText),
                    (TextView) findViewById(R.id.shoulderRotationCircuitTempText),
                    (TextView) findViewById(R.id.shoulderFlexCircuitTempText),
                    (TextView) findViewById(R.id.bicepFlexCircuitTempText),
                    (TextView) findViewById(R.id.bicepRotationCircuitTempText)
            ));
        }
    }
}
