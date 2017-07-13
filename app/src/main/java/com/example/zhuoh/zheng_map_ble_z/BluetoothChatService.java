package com.example.zhuoh.zheng_map_ble_z;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Vera on 2016/4/8.
 */
public class BluetoothChatService {
    // 调试
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    // 名社民党记录当创建服务器套接字
    private static final String NAME = "BluetoothChat";
    // 独特的是这个应用程序
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // 适配器成员
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Context mContext;
    private int mState;
    private String data = "";
    private int sig_flag=0;
    private int sig_strength=0;
    private int debug_flag=0;
    private int debug_info=0;

    private int  g_count=0;
    int freq=0;
    int ack=0;

    //常数，指示当前的连接状态
    public static final int STATE_NONE       = 0;       // 当前没有可用的连接
    public static final int STATE_LISTEN     = 1;       // 现在侦听传入的连接
    public static final int STATE_CONNECTING = 2;       // 现在开始传出联系
    public static final int STATE_CONNECTED  = 3;       // 现在连接到远程设备
    /**
     * 构造函数。准备一个新的bluetoothchat会话。
     * @param context  用户界面活动的背景
     * @param handler 一个处理程序发送邮件到用户界面活性
     */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState   = STATE_NONE;
        mHandler = handler;
        mContext =context;
    }

    /**
     * 设置当前状态的聊天连接
     * @param state  整数定义当前连接状态
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // 给新状态的处理程序，界面活性可以更新
        mHandler.obtainMessage(ConstantData.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    /**
     * 返回当前的连接状态。 */
    public synchronized int getState() {
        return mState;
    }

    /**
     * 开始聊天服务。特别acceptthread开始    开始
     * 会话听力（服务器）模式。所谓的活动onresume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");
        //取消任何线程试图建立连接
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // 启动线程来听一个bluetoothserversocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    //连接按键响应函数
    /**
     * 开始connectthread启动连接到远程设备。
     * 装置连接的蓝牙设备
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // 取消任何线程试图建立连接
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        //启动线程连接的设备
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * 开始connectedthread开始管理一个蓝牙连接
     * bluetoothsocket插座上连接了
     * 设备已连接的蓝牙设备
     */
    @SuppressWarnings("unused")
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // 取消线程完成连接
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        //取消任何线程正在运行的连接
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // 取消接受线程只因为我们要连接到一个设备
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // 启动线程管理连接和传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();


        //把名字的连接设备到Activity
        Message msg = mHandler.obtainMessage(ConstantData.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantData.DEVICE_NAME, device.getName());
        String address= device.getAddress();
        String subaddress = address.substring(12,17);
        bundle.putString(ConstantData.DEVICE_ADD, subaddress);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
        g_count=0;
        freq=0;
    }

    /**
     * 停止所有的线程
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        //创建临时对象
        ConnectedThread r;
        // 同步副本的connectedthread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        Log.i(TAG, "执行写同步");
        // 执行写同步
        r.write_byte(out);
    }

    public void close_connect() {
        //创建临时对象
        if (mState != STATE_CONNECTED) return;
        mConnectedThread.cancel();

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // 发送失败的信息带回活动
        Message msg = mHandler.obtainMessage(ConstantData.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantData.TOAST,mContext.getResources().getString(R.string.BT_disable));//"Can not connect to the bluetooth");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */

//    private Resources getResources() {
//// TODO Auto-generated method stub
//        Resources mResources = null;
//        mResources = getResources();
//        return mResources;
//    }
    private void connectionLost() {
        setState(STATE_LISTEN);
        // 发送失败的信息带回Activity
        Message msg = mHandler.obtainMessage(ConstantData.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantData.TOAST,mContext.getResources().getString(R.string.BT_lost));//"Bluetooth lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     *本线同时侦听传入的连接。它的行为
     *喜欢一个服务器端的客户端。它运行直到连接被接受
     *（或取消）。
     */
    private class AcceptThread extends Thread {
        // 本地服务器套接字
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // 创建一个新的侦听服务器套接字
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // 听服务器套接字如果我们没有连接
            while (mState != STATE_CONNECTED) {
                try {
                    // 这是一个阻塞调用和将只返回一个
                    // 成功的连接或例外
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // 如果连接被接受
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // 没有准备或已连接。新插座终止。
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * 本线在试图使传出联系
     *与设备。它径直穿过连接；或者
     *成功或失败。
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public
        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // 得到一个bluetoothsocket为与连接
            // 由于蓝牙设备
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            //总是取消的发现，因为它会减缓连接
            mAdapter.cancelDiscovery();

            // 使一个连接到bluetoothsocket
            try {
                // 这是一个阻塞调用和将只返回一个
                // 成功的连接或例外
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                //关闭这个socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // 启动服务在重新启动聆听模式
                BluetoothChatService.this.start();
                return;
            }

            // 因为我们所做的connectthread复位
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }
            // 启动连接线程
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    /**
     * 本线在连接与远程设备。
     * 它处理所有传入和传出的传输。
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // 获得bluetoothsocket输入输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "没有创建临时sockets", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        private void RecvState( byte buf[])
        {
            //Log.i(TAG, "开始返回处理数据");
            int i;
            int rst;
            //Log.i(TAG, "10:"+Integer.toString(buf.length));
            //Log.i(TAG, "buf的长度是:"+Integer.toString(buf.length));
            for(i=0; i<buf.length; i++)
            {
                //Log.v(TAG, Integer.toHexString(buf[i]));
                rst = (buf[i])&0xff;
                if(rst==0x0A||rst==0x0D){

                }else{
                    data = data+(char)rst;
                }
                //Log.v(TAG, Integer.toHexString(rst));
                if (rst==0x4F)
                {
                    g_count=1;
                    continue;
                }
                if (rst==0x4B&&g_count==1)
                {

                    /*Message msg = mHandler.obtainMessage(ConstantData.MESSAGE_SUCCESS);
                    Bundle bundle = new Bundle();
                    bundle.putString("SUCCESS","已连接");//"Can not connect to the bluetooth");
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);*/

                }

            }
            //data.replaceAll("\r|\n", "");
            if(data.endsWith("OK")){
                Log.i(TAG, data);
                if(data.contains(":")){
                    /*data = data.substring(data.indexOf(":"),data.length()-2);
                    String a[]= data.split(",");*/
                    deal_handler(data);
                    data = "";
                }else{
                    data = "";
                }
            }
        }
        void deal_handler(String str){
            Message msg = mHandler.obtainMessage(ConstantData.MESSAGE_SUCCESS);
            Bundle bundle = new Bundle();
            bundle.putString("SUCCESS",str);//"Can not connect to the bluetooth");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[66];
            int bytes;

            // 继续听InputStream同时连接
            while (true) {
                try {
                    bytes=mmInStream.read(buffer);

                    if(bytes<0)
                        break;
                    byte[] buf = new byte[bytes];
                    System.arraycopy(buffer,0,buf,0,bytes);

                    RecvState(buf);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }



        /**
         * 写输出的连接。
         * @param buffer  这是一个字节流
         */
        public void write_byte(byte[] buffer) {
            try {

                mmOutStream.write(buffer);
                Log.i(TAG, "已经执行写同步");
                // 分享发送的信息到Activity
                mHandler.obtainMessage(ConstantData.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
