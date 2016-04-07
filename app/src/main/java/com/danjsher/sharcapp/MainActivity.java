package com.danjsher.sharcapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.DatagramSocket;


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

        // Button click handlers
        final Button armStartButton = (Button)findViewById(R.id.ArmStartButton);

        armStartButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new SharcComThread().execute(
                                new ComParams(
                                        "2 1",
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
                                new ComParams(
                                        "2 0",
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
                                        (TextView) findViewById(R.id.SleeveStatusText)
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
                                new ComParams(
                                        "0",
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

        final Button calibrateBicepButton = (Button) findViewById(R.id.CalibrateBicepButton);

        calibrateBicepButton.setTag(0);

        calibrateBicepButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        final int status = (Integer) v.getTag();
                        switch (status) {
                            case (0):
                                // start a calibration task to continuously poll for data
                                new CalibrationAsyncTask().execute(
                                        // send a CALINBRATE message first
                                        new CalibrationParameters(
                                                "4",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.CalibrateButtonStatusText)
                                        ),
                                        // then send a CAL_STAGE_0 message
                                        new CalibrationParameters(
                                                "5",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.BicepFlexMinStatusText),
                                                (TextView) findViewById(R.id.BicepFlexMaxStatusText)
                                        ));
                                v.setTag(1);
                                calibrateBicepButton.setText("Finish Calibration");
                                break;
                            case (1):
                                // send stop message to stop polling and accept values
                                new SharcComThread().execute(new ComParams(
                                                "0",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.CalibrateButtonStatusText)
                                        ),
                                        new ComParams(
                                                "1 5 " + ((TextView)findViewById(R.id.BicepFlexMinStatusText)).getText().toString() + " "
                                                        + ((TextView)findViewById(R.id.BicepFlexMaxStatusText)).getText().toString(),
                                                armSocket,
                                                "192.168.23.19",
                                                12346,
                                                (TextView)findViewById(R.id.ArmStatusText)
                                        )
                                );
                                v.setTag(0);
                                calibrateBicepButton.setText("Calibrate Bicep");
                                break;
                        }
                    }
                }
        );
        final Button calibrateShoulderFlexButton = (Button) findViewById(R.id.CalibrateShoulderFlexButton);

        calibrateShoulderFlexButton.setTag(0);

        calibrateShoulderFlexButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        final int status = (Integer) v.getTag();
                        switch (status) {
                            case (0):
                                // start a calibration task to continuously poll for data
                                new CalibrationAsyncTask().execute(
                                        // send a CALINBRATE message first
                                        new CalibrationParameters(
                                                "4",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.CalibrateButtonStatusText)
                                        ),
                                        // then send a CAL_STAGE_1 message
                                        new CalibrationParameters(
                                                "6",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.ShoulderFlexAtSideStatusText),
                                                (TextView) findViewById(R.id.ShoulderFlexOutStatusText)
                                        ));
                                v.setTag(1);
                                calibrateShoulderFlexButton.setText("Finish Calibration");
                                break;
                            case (1):
                                // send stop message to stop polling and accept values
                                new SharcComThread().execute(
                                        new ComParams(
                                                "0",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(+R.id.CalibrateButtonStatusText)
                                        ),
                                        new ComParams(
                                                "1 6 " + ((TextView)findViewById(R.id.ShoulderFlexAtSideStatusText)).getText().toString() + " "
                                                + ((TextView)findViewById(R.id.ShoulderFlexOutStatusText)).getText().toString(),
                                                armSocket,
                                                "192.168.23.19",
                                                12346,
                                                (TextView)findViewById(R.id.ArmStatusText)
                                        )
                                );
                                v.setTag(0);
                                calibrateShoulderFlexButton.setText("Calibrate Shoulder Flex");
                                break;
                        }
                    }
                }
        );

        final Button calibrateShoulderRotationButton = (Button) findViewById(R.id.CalibrateShoulderRotationButton);

        calibrateShoulderRotationButton.setTag(0);

        calibrateShoulderRotationButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        final int status = (Integer) v.getTag();
                        switch (status) {
                            case (0):
                                // start a calibration task to continuously poll for data
                                new CalibrationAsyncTask().execute(
                                        // send a CALIBRATE message first
                                        new CalibrationParameters(
                                                "4",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.CalibrateButtonStatusText)
                                        ),
                                        // then send a CAL_STAGE_2 message
                                        new CalibrationParameters(
                                                "7",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.ShoulderRotationFrontStatusText),
                                                (TextView) findViewById(R.id.ShoulderRotationBackStatusText)
                                        ));
                                v.setTag(1);
                                calibrateShoulderRotationButton.setText("Finish Calibration");
                                break;
                            case (1):
                                // send stop message to stop polling and accept values
                                new SharcComThread().execute(new ComParams(
                                                "0",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (TextView) findViewById(R.id.CalibrateButtonStatusText)
                                        ),
                                        new ComParams(
                                                "1 7 " + ((TextView)findViewById(R.id.ShoulderRotationFrontStatusText)).getText().toString() + " "
                                                        + ((TextView)findViewById(R.id.ShoulderRotationBackStatusText)).getText().toString(),
                                                armSocket,
                                                "192.168.23.19",
                                                12346,
                                                (TextView)findViewById(R.id.ArmStatusText)
                                        )
                                );
                                v.setTag(0);
                                calibrateShoulderRotationButton.setText("Calibrate Shoulder Rot.");
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
}

