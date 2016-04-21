package com.danjsher.sharcapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramSocket;

public class RecordActivity extends AppCompatActivity {
    private static DatagramSocket sleeveSocket = null;
    private SharcComThread comThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // create socket for communicating with sharc sleeve
        try {
            if(sleeveSocket == null) {
                sleeveSocket = new DatagramSocket(12357);
                sleeveSocket.setReuseAddress(true);
                // set ten second time out on socket
                sleeveSocket.setSoTimeout(3000);
            }
        } catch (Exception e) {

        }

        if(comThread == null) {
            comThread = new SharcComThread(this);
        }

        comThread.execute(
                new ComParams(
                        "11",//+ ((EditText) findViewById(R.id.recordingNameTextField)).getText().toString(),
                        sleeveSocket,
                        "192.168.23.1",
                        12347,
                        (ListView) findViewById(R.id.savedRecordingsListView)
                )
        );

        registerListViewClickCallback();

        final Button recordButton = (Button) findViewById(R.id.stopRecordButton);
        recordButton.setTag(0);

        recordButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        final int status = (Integer)v.getTag();

                        switch(status) {
                            case 0:
                                new SharcComThread().execute(
                                  new ComParams(
                                          "2 " + ((EditText) findViewById(R.id.recordingNameTextField)).getText().toString(),
                                          sleeveSocket,
                                          "192.168.23.1",
                                          12347
                                  )
                                );
                                recordButton.setText("Save Recording");
                                v.setTag(1);
                                break;
                            case 1:
                                // stop recording
                                new SharcComThread().execute(
                                        new ComParams(
                                                "0",
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347
                                        )
                                );

                                // update list
                                new SharcComThread().execute(
                                        new ComParams(
                                                "11",//+ ((EditText) findViewById(R.id.recordingNameTextField)).getText().toString(),
                                                sleeveSocket,
                                                "192.168.23.1",
                                                12347,
                                                (ListView) findViewById(R.id.savedRecordingsListView)
                                        )
                                );

                                recordButton.setText("Start Recording");

                                v.setTag(0);
                                break;
                        }

                    }
                }
        );
    }

    private void registerListViewClickCallback() {
        ListView list = (ListView) findViewById(R.id.savedRecordingsListView);
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView textView = (TextView) view;
                        //String message = "you clicked " + position + ", which is string: " + textView.getText().toString();
                        //Toast.makeText(RecordActivity.this, message, Toast.LENGTH_SHORT).show();

                        new SharcComThread().execute(
                          new ComParams(
                                  "3 " + textView.getText().toString(),
                                  sleeveSocket,
                                  "192.168.23.1",
                                  12347
                          )
                        );

                        String message = "Loading recording...";
                        Toast.makeText(RecordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}
