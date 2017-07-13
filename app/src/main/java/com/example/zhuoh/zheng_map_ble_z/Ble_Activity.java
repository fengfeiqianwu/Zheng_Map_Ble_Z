package com.example.zhuoh.zheng_map_ble_z;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Ble_Activity extends Activity implements View.OnClickListener{

    private static final String TAG = "Ble";
    public final static String SER_KEY = "com.zhuoh.ser";
    /*Button*/
    public static Button btn_bl_connect;
    public static Button btn_bl_disconnect;
    public static TextView tv_con_state;
    public static Queue<String> blequeue = new LinkedList<String>();
    private String mConnectedDeviceName = null;
    private String mConnectedDeviceAdd = null;
    // 当地的蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;
    // 成员对象的聊天服务
    public static BluetoothChatService mChatService = null;

    public List<Integer> list_1 = new ArrayList<>();

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case ConstantData.MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            tv_con_state.setText(R.string.state_connected);
                            tv_con_state.append(mConnectedDeviceName+" "+mConnectedDeviceAdd);
                            ConstantData.Flag_BtConnected=true;

                            Toast.makeText(getApplicationContext(), R.string.Cation_Init, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tv_con_state.setText(R.string.state_connecting);
                            ConstantData.Flag_BtConnected=false;
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            tv_con_state.setText(R.string.state_disconnect);
                            disconnect_process();
                            break;
                    }
                    break;

                case ConstantData.MESSAGE_DEVICE_NAME:
                    // 保存该连接装置的名字
                    mConnectedDeviceName = msg.getData().getString(ConstantData.DEVICE_NAME);
                    mConnectedDeviceAdd  = msg.getData().getString(ConstantData.DEVICE_ADD);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.state_connected) + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case ConstantData.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(ConstantData.TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case ConstantData.MESSAGE_SUCCESS:
                    if(MapActivity.is_Pause_Continue){
                        String data = msg.getData().getString("SUCCESS");
                        //blequeue.offer(data);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
                        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                        String strtime = formatter.format(curDate);
                        if(MapActivity.is_Neizone){
                            MapActivity.latttt_content.setText(strtime);
                            if(data.startsWith("AT+SCELLINFO+SCELLINFO")){
                                data = data.substring(data.indexOf(":")+1,data.length()-2);
                                MapActivity.first_content.setText(data);
                            }else if(data.startsWith("AT+NCELLINFO+NCELLINFO:")){
                                data = data.substring(data.indexOf(":")+1,data.length()-2);
                                data = data.replace("+NCELLINFO:","*");
                                Log.i(TAG,data);
                                String a[] = data.split("\\*");
                                MapActivity.second_content.setText(a[0]);
                                MapActivity.third_content.setText(a[1]);
                                MapActivity.fourth_content.setText(a[2]);
                                MapActivity.fifth_content.setText(a[3]);
                                MapActivity.sixth_content.setText(a[4]);
                                MapActivity.seventh_content.setText(a[5]);
                            }else if(data.startsWith("AT+BSINFO+BSINFO")){
                                data = data.substring(data.indexOf(":")+1,data.length()-2);
                                if(data.contains("+BSINFO:")){
                                    String a[] = data.split("\\+BSINFO:");
                                    switch (a.length){
                                        case 2:
                                            MapActivity.first_content.setText(" "+a[0]);
                                            MapActivity.second_content.setText(" "+a[1]);
                                            break;
                                        case 3:
                                            MapActivity.first_content.setText(" "+a[0]);
                                            MapActivity.second_content.setText(" "+a[1]);
                                            MapActivity.third_content.setText(" "+a[2]);
                                            break;
                                    }
                                }else{
                                    MapActivity.first_content.setText(" "+data);
                                }
                            }
                        }else{
                            if(data.startsWith("AT+SCELLINFO+SCELLINFO")){
                                data = data.substring(data.indexOf(":")+1,data.length()-2);
                                String a[]= data.split(",");
                                MapActivity.lattime_content.setText(strtime);
                                MapActivity.mcc_content.setText(a[1]);
                                MapActivity.mnc_content.setText(a[2]);
                                MapActivity.lac_content.setText(a[3]);
                                MapActivity.ci_content.setText(a[4]);
                                if(MapActivity.is_loc_data){
                                    MapActivity.tempdata.put("strtime",strtime);
                                    MapActivity.tempdata.put("cellMode",a[0]);
                                    MapActivity.tempdata.put("strmcc",a[1]);
                                    MapActivity.tempdata.put("strmnc",a[2]);
                                    MapActivity.tempdata.put("strlac",a[3]);
                                    MapActivity.tempdata.put("strcid",a[4]);
                                    MapActivity.tempdata.put("strchannel",a[5]);
                                    MapActivity.tempdata.put("strrxlevel",a[7]);
                                    MapActivity.tempdata.put("strbid","0");
                                    //MapActivity.is_loc_data = false;
                                }
                            }else if(data.startsWith("AT+BSINFO+BSINFO")){
                                data = data.substring(data.indexOf(":")+1,data.length()-2);
                                String a[]= data.split(",");
                                MapActivity.lattime_content.setText(strtime);
                                MapActivity.sid_content.setText(a[0]);
                                MapActivity.nid_content.setText(a[1]);
                                MapActivity.bid_content.setText(a[2]);
                            }
                        }


                        /*MapActivity.mcc.setText(a[1]);//移动国家号码
                        MapActivity.mnc.setText(a[2]);//移动网络号码
                        MapActivity.tac.setText(a[3]);//位置区信息
                        MapActivity.cid.setText(a[4]);//小区信息
                        MapActivity.mrxLevel.setText(a[7]);//信号强度
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
                        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                        String str = formatter.format(curDate);
                        MapActivity.time.setText(str);*/
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_connect);
        init_ui();
    }

    void init_ui() {
        // 得到当地的蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        tv_con_state   = (TextView)findViewById(R.id.tv_connect_state);
        btn_bl_connect = (Button)findViewById(R.id.bl_connect);
        btn_bl_disconnect = (Button)findViewById(R.id.bl_disconnect);
        btn_bl_connect.setOnClickListener(this);
        btn_bl_disconnect.setOnClickListener(this);
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.disconnect, Toast.LENGTH_LONG).show();
            //finish();
        }
    }

    /**
     * 主界面按钮相应点击事件
     *
     * @param v 被点击的按钮
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //执行发现周边蓝牙设备的操作
            case R.id.bl_connect:
                //连接
                if (!mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.enable();//若蓝牙没打开，打开蓝牙
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, ConstantData.REQUEST_CONNECT_DEVICE);
                return ;
            case R.id.bl_disconnect:
                //断开
                //Log.v(TAG, "R.id.action_disconnect");//音频文件加载
                if (mChatService==null)
                {
                    return ;
                }
                mChatService.stop();
                disconnect_process();
                //Log.v(TAG, "battery_display(0)");//音频文件加载
                return ;
            default:
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();//打开蓝牙无提示
            /*以下两句打开蓝牙有提示*/
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent,ConstantData.REQUEST_ENABLE_BT)
        } else {
            if (mChatService == null){
                setupChat();
                Log.i(TAG, "启动服务");
            }

        }
    }
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        //初始化bluetoothchatservice执行蓝牙连接
        mChatService = new BluetoothChatService(this, mHandler);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //if (D)
        Log.v(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case ConstantData.REQUEST_CONNECT_DEVICE:
                // 当devicelistactivity返回连接装置
                if (resultCode == Activity.RESULT_OK) {
                    if (mChatService == null) {
                        setupChat();
                    }

                    // 获得设备地址
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 把蓝牙设备对象
                    Log.v(TAG, "BT address=" + address);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // 试图连接到装置
                    //Log.v(TAG, "device="+device );
                    if (device == null)
                        Log.v(TAG, "device=null");
                    Log.v(TAG, "device=" + device);
                    if (mChatService == null) {
                        Log.v(TAG, "mChatService==null");
                        return;
                    }

                    mChatService.connect(device);
                }
                break;
            case ConstantData.REQUEST_ENABLE_BT:
                // 当请求启用蓝牙返回
                if (resultCode == Activity.RESULT_OK) {
                    // 蓝牙已启用，所以建立一个聊天会话
                    setupChat();
                } else {
                    // 用户未启用蓝牙或发生错误
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    //finish();
                }
        }
    }

    void disconnect_process()
    {
        list_1.clear();
    }


    /*protected void onDestroy() {
        Log.v(TAG, "onDestroy()");

        super.onDestroy();
//        if (mChatService != null)
//        {
//            mChatService.close_connect();
//            mChatService.stop();
//        }
        //mTts.shutdown();
    }*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
