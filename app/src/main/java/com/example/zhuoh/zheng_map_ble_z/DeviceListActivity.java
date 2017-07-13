package com.example.zhuoh.zheng_map_ble_z;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Vera on 2016/4/19.
 */
public class DeviceListActivity extends Activity {
    // 调试
    private static final int REQUEST_FINE_LOCATION=0;
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    // 返回别的意图
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    // 适配器
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    TextView tv_paired_device,tv_new_device;
    Button btn_scan;
    ListView ListV_paried,ListV_new;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 指定窗口样式
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);
        init();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,filter);

        if (Build.VERSION.SDK_INT >= 23) {
            //判断是否有权限
            int checkCallPhonePermission =  ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if(checkCallPhonePermission !=  PackageManager.PERMISSION_GRANTED){
                //判断是否需要 向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS);

                ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE_LOCATION);
                //return;
            }
        }
    }

    void init()
    {
        tv_new_device=(TextView)findViewById(R.id.tv_new_device);
        tv_paired_device=(TextView)findViewById(R.id.tv_paired_device);
        // 初始化数组适配器。一个已配对装置和
        //一个新发现的设备
        mPairedDevicesArrayAdapter = new ArrayAdapter<>(this,android.R.layout.test_list_item);
        mNewDevicesArrayAdapter    = new ArrayAdapter<>(this,android.R.layout.test_list_item);

        //寻找和建立配对设备列表
        ListV_paried   = (ListView) findViewById(R.id.listV_paried_device);
        ListV_paried.setAdapter(mPairedDevicesArrayAdapter);

        ListV_paried.setOnItemClickListener(mDeviceClickListener);

        // 寻找和建立为新发现的设备列表
        ListV_new = (ListView) findViewById(R.id.listV_new_device);
        ListV_new.setAdapter(mNewDevicesArrayAdapter);
        ListV_new.setOnItemClickListener(mDeviceClickListener);



        // 结果取消如果用户备份
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mNewDevicesArrayAdapter.clear();
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // 获取本地蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // 得到一套目前配对设备
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            tv_paired_device.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices)
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        else
        {
            tv_paired_device.setVisibility(View.GONE);
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }

    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mBtAdapter != null)
        {
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }

    /**
     * 发现与bluetoothadapter启动装置
     */
    private void doDiscovery()
    {
        if (D) Log.d(TAG, "doDiscovery()");
        // 显示扫描的称号
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        // 打开新设备的字幕
        tv_new_device.setVisibility(View.VISIBLE);

        // 如果我们已经发现，阻止它
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();}
        // 要求从bluetoothadapter发现
        mBtAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {
            mBtAdapter.cancelDiscovery();
            //获得设备地址，这是近17字的
            //视图
            String info = ((TextView) v).getText().toString();
            Log.v(TAG, "BT info length = "+info.length()+ "info ="+info );
            if (info.length()<=17) {
                Log.v(TAG, "lenth<=17" );
                return;
            }

            String address = info.substring(info.length() - 17);
            Log.v(TAG, "BT address="+address );
            //创建结果意图和包括地址
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            //结果，完成这项活动
            setResult(Activity.RESULT_OK, intent);
            finish();

        }
    };


    // 该broadcastreceiver监听设备和
    // 变化的标题时，发现完成
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // 当发现设备
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //把蓝牙设备对象的意图
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果它已经配对，跳过它，因为它的上市
                // 早已
                if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    for (int index=0;index<mNewDevicesArrayAdapter.getCount();index++)
                    {
                        if (mNewDevicesArrayAdapter.getItem(index).equals(device.getName() + "\n" + device.getAddress()))
                        {
                            return;
                        }
                    }

                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                //当发现后，改变活动名称
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0)
                {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                btn_scan.setVisibility(View.VISIBLE);

            }
        }
    };

}
