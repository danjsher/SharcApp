package com.danjsher.sharcapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by dan on 3/26/2016.
 */
public class StatPollService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public StatPollService() {
        super("SharcStatPollService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // receive data from intent on main thread
        String dataString = workIntent.getDataString();

        // create udpSocket and connect to raspberry pi
        Integer counter = 0;
        // do some work now...
        while (true) {
            // contact raspberry pi and get results

            counter++;

            // send results to main thread for display
            String status = counter.toString();
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, status);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
