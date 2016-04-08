package com.danjsher.sharcapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.DatagramSocket;

public class TemperatureActivity extends AppCompatActivity {
    private DatagramSocket armSocket = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);

        // Button Click Handlers

        Button startMonitoringButton = (Button) findViewById(R.id.startMonitoringButton);

        startMonitoringButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new CalibrationAsyncTask().execute(
                                new CalibrationParameters(
                                        "",
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
                                 )
                        );
                    }
                }
        );
    }



}
