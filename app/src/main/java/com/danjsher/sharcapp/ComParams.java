package com.danjsher.sharcapp;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.net.DatagramSocket;

public class ComParams {
    public DatagramSocket udpSocket; //socket to send message on
    public String message;           // message code to send
    public String ipAddr;            // ip address to send to
    public int port;                 // destination port
    public String response;          // response from server
    public TextView statusTextView;  // text view to update
    public ListView mListView;

    public ComParams(String m, DatagramSocket s, String ip, int p, TextView v) {
        udpSocket = s;
        message   = m;
        ipAddr    = ip;
        port      = p;
        response  = "";
        statusTextView = v;
    }

    public ComParams(String m, DatagramSocket s, String ip, int p, ListView v) {
        udpSocket = s;
        message   = m;
        ipAddr    = ip;
        port      = p;
        response  = "";
        mListView = v;
    }

    public ComParams(String m, DatagramSocket s, String ip, int p) {
        udpSocket = s;
        message   = m;
        ipAddr    = ip;
        port      = p;
        response  = "";
    }
}