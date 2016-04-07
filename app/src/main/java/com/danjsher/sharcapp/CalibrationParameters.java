package com.danjsher.sharcapp;

import android.widget.TextView;

import java.net.DatagramSocket;

/**
 * Created by dan on 4/6/2016.
 */
public class CalibrationParameters {
    public DatagramSocket udpSocket; //socket to send message on
    public String message;           // message code to send
    public String ipAddr;            // ip address to send to
    public int port;                 // destination port
    public String response;          // response from peer
    public TextView[] statusTextView;  // text views to update

    public CalibrationParameters (String m, DatagramSocket s, String ip, int p, TextView... v) {
        udpSocket = s;
        message   = m;
        ipAddr    = ip;
        port      = p;
        response  = "";
        statusTextView = v;
    }
}
