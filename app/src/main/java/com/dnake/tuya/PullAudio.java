package com.dnake.tuya;

import android.util.Log;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.tuya.smart.aiipc.ipc_sdk.api.Common;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PullAudio {
    public static final int TxPort = 14032;
    public static final int RxPort = 14033;
    public static DatagramSocket socket;
    public static long ts = 0;

    public static Boolean ok = false;
    static BufferedOutputStream bos;

    public static void start() {
        try {
            socket = new DatagramSocket(RxPort, InetAddress.getByName("127.0.0.1"));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ts = 0;
        dmsg req = new dmsg();
        dxml p = new dxml();
        p.setInt("/params/ex", 1);
        req.to("/talk/start", p.toString());
        ok = true;

        AudioThread d = new AudioThread();
        Thread thread = new Thread(d);
        thread.start();
    }

    public static void stop() {
        ok = false;
        dmsg req = new dmsg();
        req.to("/talk/audio/ext/stop", null);

    }

    public static void send(byte[] data, int length) {
        if (socket == null)
            return;

        try {


            DatagramPacket p = new DatagramPacket(data, length, InetAddress.getByName("127.0.0.1"), TxPort);
            socket.send(p);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class AudioThread implements Runnable {
        @Override
        public void run() {
            byte[] data = new byte[320];
            IMediaTransManager transManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);


            while (ok) {
                DatagramPacket dp = new DatagramPacket(data, data.length);
                try {

                    socket.receive(dp);
                    if (dp.getLength() > 0) {
                        transManager.pushMediaStream(Common.ChannelIndex.E_CHANNEL_AUDIO, 0, dp.getData());

                        ts++;
                        if (!TuYa_ipc.speakStatus) {
                            byte[] tt = new byte[320];
                            send(tt, tt.length);
                        }
                    }

                } catch (Exception e) {
                    stop();
                    e.printStackTrace();
                }
            }
            if (socket != null)
                socket.close();
            socket = null;
        }
    }
}
