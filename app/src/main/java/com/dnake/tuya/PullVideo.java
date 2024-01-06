package com.dnake.tuya;

import android.util.Log;

import com.tuya.smart.aiipc.ipc_sdk.api.Common;
import com.tuya.smart.aiipc.ipc_sdk.api.IMediaTransManager;
import com.tuya.smart.aiipc.ipc_sdk.service.IPCServiceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class PullVideo {
    private static PullVideo instance = null;
    public static Socket socket;
    public static Boolean ok = false;
    private final static int VIDEO_BUF_SIZE = 1024 * 10;
    private byte[] SPS;
    private byte[] PPS;
    private BufferedInputStream bufferedInputStream;
    private ByteArrayOutputStream byteArrayOutputStream;
    private InputStream videoFis;
    IMediaTransManager transManager;
    public static Boolean isFirst = false;

    enum FrameType {
        IDR,
        P,
        PPS,
        SPS
    }


    class Frame {
        FrameType type;
        byte[] data;
    }

    public static PullVideo getInstance() {
        if (instance == null) {
            instance = new PullVideo();
        }
        return instance;
    }


    public void start() {

        if (ok)
            return;
        try {
            socket = new Socket(InetAddress.getLocalHost(), 13033);
        } catch (IOException e) {

            e.printStackTrace();
        }
        ok = true;
        isFirst = true;
        VideoThread d = new VideoThread();
        Thread thread = new Thread(d);
        thread.start();


    }

    public void stop() {

        byte[] data = new byte[52 * 1000];
        data[3] = 1;
        data[4] = 101;
        data[5] = (byte) 136;
        data[6] = (byte) 132;
        data[8] = (byte) 9;
        data[9] = (byte) 255;
        if (transManager != null)
            transManager.pushMediaStream(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.NAL_TYPE.NAL_TYPE_IDR, data);
        ok = false;
    }


    public class VideoThread implements Runnable {
        @Override
        public void run() {
            if (socket != null && socket.isConnected()) {
                try {
                    videoFis = socket.getInputStream();
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    bufferedInputStream = new BufferedInputStream(videoFis);
                    transManager = IPCServiceManager.getInstance().getService(IPCServiceManager.IPCService.MEDIA_TRANS_SERVICE);
                    Frame frame;
                    while (ok) {
                        frame = takeFrame();

                        if (frame == null || frame.type == null) {
                            continue;
                        }


                        if (frame.type == FrameType.IDR) {
                            isFirst = false;
                             transManager.pushMediaStream(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.NAL_TYPE.NAL_TYPE_IDR, frame.data);
                        } else {
                             transManager.pushMediaStream(Common.ChannelIndex.E_CHANNEL_VIDEO_MAIN, Common.NAL_TYPE.NAL_TYPE_PB, frame.data);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        videoFis.close();
                        byteArrayOutputStream.close();
                        bufferedInputStream.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


        }
    }

    private Frame takeFrame() throws Exception {

        while (ok) {
            Frame frame = takeData();
            if (frame != null) {
                if (frame.type == FrameType.SPS) {
                    SPS = frame.data;
                    continue;
                } else if (frame.type == FrameType.PPS) {
                    PPS = frame.data;
                    continue;
                } else {
                    if (frame.type == FrameType.IDR) {
                        if (SPS != null && PPS != null) {
                            byte[] data = new byte[frame.data.length + SPS.length + PPS.length];
                            System.arraycopy(SPS, 0, data, 0, SPS.length);
                            System.arraycopy(PPS, 0, data, SPS.length, PPS.length);
                            System.arraycopy(frame.data, 0, data, SPS.length + PPS.length, frame.data.length);
                            frame.data = data;
                        }
                    }
                }
            }
            return frame;
        }
        return null;
    }

    private static void dealType(int type, Frame frame) {
        if ((type & 31) == 5) {
            frame.type = FrameType.IDR;
        } else if ((type & 31) == 7) {
            frame.type = FrameType.SPS;
            //SPS
        } else if ((type & 31) == 8) {
            //PPS
            frame.type = FrameType.PPS;
        } else if ((type & 31) == 1) {
            frame.type = FrameType.P;
        }
    }

    private static int isDiv(byte[] data, int start, Frame frame) {
        int type;
        int ret;
        int i = 0;
        for (; i + start + 4 < data.length; i++) {
            int idx = start + i;
            if (data[idx] == 0 && data[idx + 1] == 0 && data[idx + 2] == 1) {
                if (i == 0) {
                    i += 3;
                    type = data[idx + 3];
                    dealType(type, frame);
                    continue;
                }
                return idx;
            }
            if (data[idx] == 0 && data[idx + 1] == 0 && data[idx + 2] == 0 && data[idx + 3] == 1) {
                if (i == 0) {
                    i += 4;
                    type = data[idx + 4];
                    dealType(type, frame);
                    continue;
                }
                return idx;
            }
        }
        ret = i - data.length;


        return ret;
    }


    private Frame takeData() throws Exception {
        Frame frame = new Frame();
        int pos = 0;
        byte[] buffer = new byte[VIDEO_BUF_SIZE];
        while (ok) {
            int len = bufferedInputStream.read(buffer, 0, VIDEO_BUF_SIZE);
            if (len <= 0) {
                videoFis.reset();
                len = bufferedInputStream.read(buffer, 0, VIDEO_BUF_SIZE);
            }
            byteArrayOutputStream.write(buffer, 0, len);
            byte[] data = byteArrayOutputStream.toByteArray();
            if ((pos = isDiv(data, pos, frame)) > 0) {
                frame.data = new byte[pos];
                System.arraycopy(data, 0, frame.data, 0, pos);
                byteArrayOutputStream.reset();
                byteArrayOutputStream.write(data, pos, data.length - pos);
                break;
            } else {
                pos += data.length;
            }
        }
        return frame;
    }
}
