package com.example.zhuoh.zheng_map_ble_z;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.TraceOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.example.zhuoh.zheng_map_ble_z.ConstantData.TOAST;
import static com.example.zhuoh.zheng_map_ble_z.ConstantData.s;
import static com.example.zhuoh.zheng_map_ble_z.ConstantData.web;

public class MapActivity extends AppCompatActivity implements View.OnClickListener,LocationSource, AMapLocationListener{

    /*界面*/
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mode_state;
    private ImageView ivRunningMan;
    private AnimationDrawable mAnimationDrawable;
    MyBroadcastReceiver mBroadcastReciver;
    private ToggleButton btn_mode;
    private LinearLayout linearLayout_zone;
    private LinearLayout linearLayout_roadtest;
    public static Boolean is_Neizone = false;
    private Boolean is_Start = false;
    SharedPreferences settings;

    /*数据库*/
    private SQLiteDatabase db;
    /*地图*/
    public static Boolean drawMap = false;
    private MapView mMapView;
    private AMap mAMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private PolylineOptions mPolyoptions;
    private Polyline mpolyline;
    private List<TraceOverlay> mOverlayList = new ArrayList<TraceOverlay>();
    private TraceOverlay mTraceoverlay;
    private TextView mlat;
    private TextView mlon;
    private TextView morder;
    public static Queue<String> locqueue = new LinkedList<String>();
    public static Map<String,Object> tempdata = new HashMap();
    public static Boolean is_loc_data = false;

    /*蓝牙*/
    private static final String TAG = "Map";
    /*路测*/
    public static TextView lac_content;
    public static TextView mcc_content;
    public static TextView mnc_content;
    public static TextView sid_content;
    public static TextView ci_content;
    public static TextView lattime_content;
    public static TextView nid_content;
    public static TextView bid_content;
    /*邻区*/
    public static TextView latttt_content;
    public static TextView first_content;
    public static TextView second_content;
    public static TextView third_content;
    public static TextView fourth_content;
    public static TextView fifth_content;
    public static TextView sixth_content;
    public static TextView seventh_content;

    public static Button continu;
    public static Button pause;
    public static Button select_web;
    private static Button btnfourmix;
    private static Button btntwomix;
    private static Button btnmobilemix;
    private static Button btnunicommix;
    private static Button btntelcommix;
    private static Button btnmobiletwo;
    private static Button btnunicomtwo;
    private static Button btntelcomtwo;
    private static Button btnmobilethree;
    private static Button btnunicomthree;
    private static Button btnmobilefour;
    private static Button btnunicomfour;
    private static Button btntelcomfour;
    public static Boolean is_Toast = false;
    public static Boolean is_Pause_Continue = true;
    private static int LTE_COUNT = -1;
    private static int GSM_COUNT = 0;
    private static int MOBILE_COUNT = 0;
    private static int UNICOM_COUNT = 0;
    private static int TELECOM_COUNT = 0;
    private static int WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
    private static int WEB_THREE_TELTWO_NEI_COUNT = -1;
    private static int WEB_FOUR_TWO_COUNT = -2;
    private static int WEB_THREE_TELTWO_COUNT = -1;
    private final int MUTI_CHOICE_DIALOG = 1;
    private MyThread thread;
    Spinner spinnerWeb;
    ArrayAdapter<String> adapterWeb;
    boolean[] selected = new boolean[]{false,false,false,false,false,false,false,false};
    public UserDao userDao;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantData.MESSAGE_DEAL_UI:
                    Log.i(TAG,msg.getData().getString("DEAL_UI"));
                    //morder.setText(msg.getData().getString("DEAL_UI"));
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        registerBroadcastReceiver();/*注册广播通知,来自于MainService的消息*/
        init_ui();

        initpolyline();
        inittoolbar();

    }
    void inittoolbar(){
        //京东RunningMan动画效果，和本次Toolbar无关
        //mAnimationDrawable = (AnimationDrawable) ivRunningMan.getBackground();
        toolbar.setTitle("");//设置Toolbar标题
        setSupportActionBar(toolbar);
//        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close) {
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    void init_ui() {
        /*界面*/
        /*路测*/
        lac_content = (TextView)findViewById(R.id.lac_content);
        mcc_content = (TextView)findViewById(R.id.mcc_content);
        mnc_content= (TextView)findViewById(R.id.mnc_content);
        sid_content = (TextView)findViewById(R.id.sid_content);
        ci_content = (TextView)findViewById(R.id.ci_content);
        lattime_content = (TextView)findViewById(R.id.lattime_content);
        nid_content = (TextView)findViewById(R.id.nid_content);
        bid_content = (TextView)findViewById(R.id.bid_content);
        /*邻区*/
        latttt_content = (TextView)findViewById(R.id.latttt_content);
        first_content = (TextView)findViewById(R.id.first_content);
        second_content = (TextView)findViewById(R.id.second_content);
        third_content = (TextView)findViewById(R.id.third_content);
        fourth_content = (TextView)findViewById(R.id.fourth_content);
        fifth_content = (TextView)findViewById(R.id.fifth_content);
        sixth_content = (TextView)findViewById(R.id.sixth_content);
        seventh_content = (TextView)findViewById(R.id.seventh_content);
        settings = getSharedPreferences("MapSetting", Context.MODE_PRIVATE);

        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_left);

        btnfourmix = (Button)findViewById(R.id.fourmix);
        btntwomix = (Button)findViewById(R.id.twomix);
        btnmobilemix = (Button)findViewById(R.id.mobilemix);
        btnunicommix = (Button)findViewById(R.id.unicommix);
        btntelcommix = (Button)findViewById(R.id.telcommix);
        btnmobiletwo = (Button)findViewById(R.id.mobiletwo);
        btnunicomtwo = (Button)findViewById(R.id.unicomtwo);
        btntelcomtwo = (Button)findViewById(R.id.telcomtwo);
        btnmobilethree = (Button)findViewById(R.id.mobilethree);
        btnunicomthree = (Button)findViewById(R.id.unicomthree);
        btnmobilefour = (Button)findViewById(R.id.mobilefour);
        btnunicomfour = (Button)findViewById(R.id.unicomfour);
        btntelcomfour = (Button)findViewById(R.id.telcomfour);
        linearLayout_zone = (LinearLayout)findViewById(R.id.zone);
        linearLayout_roadtest = (LinearLayout)findViewById(R.id.roadtest);
        btnfourmix.setOnClickListener(this);
        btntwomix.setOnClickListener(this);
        btnmobilemix.setOnClickListener(this);
        btnunicommix.setOnClickListener(this);
        btntelcommix.setOnClickListener(this);
        btnmobiletwo.setOnClickListener(this);
        btnunicomtwo.setOnClickListener(this);
        btntelcomtwo.setOnClickListener(this);
        btnmobilethree.setOnClickListener(this);
        btnunicomthree.setOnClickListener(this);
        btnmobilefour.setOnClickListener(this);
        btnunicomfour.setOnClickListener(this);
        btntelcomfour.setOnClickListener(this);
        /*pause = (Button)findViewById(R.id.pause);
        continu = (Button)findViewById(R.id.continu);*/
        //select_web = (Button)findViewById(R.id.select_web);
        //pause.setOnClickListener(this);
        //continu.setOnClickListener(this);
        //select_web.setOnClickListener(this);
        /*spinnerWeb = (Spinner) findViewById(R.id.spinner_web);
        adapterWeb = new MyAdapterBlue(this, ConstantData.web_array);*/
        //spinnerWeb.setAdapter(adapterWeb);
        //spinnerWeb.setOnItemSelectedListener(new SpinnerWebSelectedListener());
        /*地图*/
        /*mlat = (TextView)findViewById(R.id.lat_content);
        mlon = (TextView)findViewById(R.id.lon_content);
        mrxLevel = (TextView)findViewById(R.id.rxLevel_content);
        morder = (TextView)findViewById(R.id.order_content);
        mcc = (TextView)findViewById(R.id.mcc_content);
        mnc = (TextView)findViewById(R.id.mnc_content);
        tac = (TextView)findViewById(R.id.tac_content);
        cid = (TextView)findViewById(R.id.cid_content);
        time = (TextView)findViewById(R.id.time_content);*/
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            setUpMap();
        }

        btn_mode = (ToggleButton)findViewById(R.id.btn_mode);
        is_Neizone = settings.getBoolean("is_Neizone",false);
        if(is_Neizone){
            web = settings.getInt("web",16);
            btn_mode.toggle();
            btnfourmix.setEnabled(false);
            btntwomix.setEnabled(false);
            btnmobilemix.setEnabled(false);
            btnunicommix.setEnabled(false);
            btntelcommix.setEnabled(false);
            linearLayout_zone.setVisibility(View.VISIBLE);
            linearLayout_roadtest.setVisibility(View.GONE);
        }else{
            web = settings.getInt("web",1);
        }
            String str_mode_state="";
            mode_state = (TextView)findViewById(R.id.mode_state);
            switch (web){
                case 1:
                    str_mode_state = "4G混合";
                    break;
                case 2:
                    str_mode_state = "2G混合";
                    break;
                case 3:
                    str_mode_state = "移动全网";
                    break;
                case 4:
                    str_mode_state = "联通全网";
                    break;
                case 5:
                    str_mode_state = "电信全网";
                    break;
                case 6:
                    str_mode_state = "移动4G";
                    break;
                case 7:
                    str_mode_state = "移动3G";
                    break;
                case 8:
                    str_mode_state = "移动2G";
                    break;
                case 9:
                    str_mode_state = "联通4G";
                    break;
                case 10:
                    str_mode_state = "联通3G";
                    break;
                case 11:
                    str_mode_state = "联通2G";
                    break;
                case 12:
                    str_mode_state = "电信4G";
                    break;
                case 13:
                    str_mode_state = "电信2G";
                    break;
                case 14:
                    str_mode_state = "移动4G";
                    break;
                case 15:
                    str_mode_state = "移动3G";
                    break;
                case 16:
                    str_mode_state = "移动2G";
                    break;
                case 17:
                    str_mode_state = "联通4G";
                    break;
                case 18:
                    str_mode_state = "联通3G";
                    break;
                case 19:
                    str_mode_state = "联通2G";
                    break;
                case 20:
                    str_mode_state = "电信4G";
                    break;
                case 21:
                    str_mode_state = "电信2G";
                    break;
            }
            mode_state.setText(str_mode_state);


        btn_mode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(web < 6){
                    Toast.makeText(getApplicationContext(),R.string.switchnei,Toast.LENGTH_SHORT).show();
                    btn_mode.toggle();
                }else{
                    stop();
                    is_Start = false;
                    if(web>5&&web<14){
                        web += 8;
                    }else if(web>13&&web<22){
                        web -= 8;
                    }
                    settings.edit().putInt("web",web).apply();
                    if(btn_mode.isChecked()){
                        Toast.makeText(getApplicationContext(),"邻区只能使用单模式",Toast.LENGTH_SHORT).show();
                        is_Neizone = true;
                        settings.edit().putBoolean("is_Neizone",is_Neizone).apply();
                        btnfourmix.setEnabled(false);
                        btntwomix.setEnabled(false);
                        btnmobilemix.setEnabled(false);
                        btnunicommix.setEnabled(false);
                        btntelcommix.setEnabled(false);
                        linearLayout_zone.setVisibility(View.VISIBLE);
                        linearLayout_roadtest.setVisibility(View.GONE);
                    }else{
                        is_Neizone = false;
                        settings.edit().putBoolean("is_Neizone",is_Neizone).apply();
                        btnfourmix.setEnabled(true);
                        btntwomix.setEnabled(true);
                        btnmobilemix.setEnabled(true);
                        btnunicommix.setEnabled(true);
                        btntelcommix.setEnabled(true);
                        linearLayout_zone.setVisibility(View.GONE);
                        linearLayout_roadtest.setVisibility(View.VISIBLE);
                    }
                }

            }
        });
        /*btn = (ToggleButton) findViewById(R.id.locationbtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn.isChecked()) {
                    mAMap.clear(true);
                    if (!ConstantData.Flag_BtConnected)
                        Toast.makeText(getApplicationContext(),R.string.Cation_Disconnected+"只能开始画轨迹", Toast.LENGTH_SHORT).show();
                    else{
                        is_Toast = true;
                        start();
                    }
                } else {
                    stop();
                    mOverlayList.add(mTraceoverlay);
                }
            }
        });
        mTraceoverlay = new TraceOverlay(mAMap);*/
    }

    /**
     * 主界面按钮相应点击事件
     *
     * @param v 被点击的按钮
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fourmix:
                mode_state.setText("4G混合");
                web = 1;
                LTE_COUNT = -1;
                break;
            case R.id.twomix:
                mode_state.setText("2G混合");
                web = 2;
                GSM_COUNT = 0;
                break;
            case R.id.mobilemix:
                mode_state.setText("移动全网");
                web = 3;
                MOBILE_COUNT = 0;
                break;
            case R.id.unicommix:
                mode_state.setText("联通全网");
                web = 4;
                UNICOM_COUNT = 0;
                break;
            case R.id.telcommix:
                mode_state.setText("电信全网");
                web = 5;
                TELECOM_COUNT = 0;
                break;
            case R.id.mobilefour:
                mode_state.setText("移动4G");
                if (is_Neizone) {
                    web = 14;
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
                } else {
                    web = 6;
                    WEB_FOUR_TWO_COUNT = -2;
                }
                break;
            case R.id.mobilethree:
                mode_state.setText("移动3G");
                if (is_Neizone) {
                    web = 15;
                    WEB_THREE_TELTWO_NEI_COUNT = -1;
                } else {
                    web = 7;
                    WEB_THREE_TELTWO_COUNT = -1;
                }
                break;
            case R.id.mobiletwo:
                mode_state.setText("移动2G");
                if (is_Neizone) {
                    web = 16;
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
                } else {
                    web = 8;
                    WEB_FOUR_TWO_COUNT = -2;
                }
                break;
            case R.id.unicomfour:
                mode_state.setText("联通4G");
                if (is_Neizone) {
                    web = 17;
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
                } else {
                    web = 9;
                    WEB_FOUR_TWO_COUNT = -2;
                }
                break;
            case R.id.unicomthree:
                mode_state.setText("联通3G");
                if (is_Neizone) {
                    web = 18;
                    WEB_THREE_TELTWO_NEI_COUNT = -1;
                } else {
                    web = 10;
                    WEB_THREE_TELTWO_COUNT = -1;
                }
                break;
            case R.id.unicomtwo:
                mode_state.setText("联通2G");
                if (is_Neizone) {
                    web = 19;
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
                } else {
                    web = 11;
                    WEB_FOUR_TWO_COUNT = -2;
                }
                break;
            case R.id.telcomfour:
                mode_state.setText("电信4G");
                if (is_Neizone) {
                    web = 20;
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
                } else {
                    web = 12;
                    WEB_FOUR_TWO_COUNT = -2;
                }
                break;
            case R.id.telcomtwo:
                mode_state.setText("电信2G");
                if (is_Neizone) {
                    web = 21;
                    WEB_THREE_TELTWO_NEI_COUNT = -1;
                } else {
                    web = 13;
                    WEB_THREE_TELTWO_COUNT = -1;
                }
                break;
            default:
                break;
        }
        settings.edit().putInt("web",web).apply();
        if(is_Start){
            stop();
            start();
        }
        mDrawerLayout.closeDrawers();
    }
    class MyThread extends Thread {
        public boolean stop;
        public void run() {
            while (!stop) {
                // 处理功能
                send_bd_msg(web);
                // 通过睡眠线程来设置定时时间
                try {
                    switch (web){
                        case ConstantData.WEB_LTE:
                            if(LTE_COUNT - 1 == -1){
                                Log.v(TAG,Integer.toString(LTE_COUNT));
                                Thread.sleep(3000);
                            }else if(LTE_COUNT%6==1||LTE_COUNT%6==3||LTE_COUNT%6==5){
                                Thread.sleep(2000);
                            }else{
                                Thread.sleep(1000);
                            }
                            break;
                        case ConstantData.WEB_GSM:
                            Thread.sleep(6000);
                            break;
                        case ConstantData.WEB_MOBILE:
                            if(MOBILE_COUNT % 8 == 0||MOBILE_COUNT % 8 == 3||MOBILE_COUNT % 8 ==5){
                                Thread.sleep(5000);
                            }else if(MOBILE_COUNT % 8 == 1||MOBILE_COUNT % 8 == 6){
                                Thread.sleep(5000);
                            }else{
                                Thread.sleep(5000);
                            }
                            break;
                        case ConstantData.WEB_UNICOM:
                            if(UNICOM_COUNT % 8 == 0||UNICOM_COUNT % 8 == 3||UNICOM_COUNT % 8 ==5){
                                Thread.sleep(5000);
                            }else if(UNICOM_COUNT % 8 == 1||UNICOM_COUNT % 8 == 6){
                                Thread.sleep(5000);
                            }else{
                                Thread.sleep(5000);
                            }
                            break;
                        case ConstantData.WEB_TELECOM:
                            if(TELECOM_COUNT % 5 == 0||TELECOM_COUNT % 8 == 3){
                                Thread.sleep(5000);
                            }else if(TELECOM_COUNT % 5 == 1){
                                Thread.sleep(5000);
                            }else{
                                Thread.sleep(5000);
                            }
                            break;
                        case ConstantData.MOBILE_FOUR_NEI:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.MOBILE_THREE_NEI:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.MOBILE_TWO_NEI:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.UNICOM_FOUR_NEI:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.UNICOM_THREE_NEI:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.UNICOM_TWO_NEI:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.TELCOM_FOUR_NEI:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.TELCOM_TWO_NEI:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.WEB_MOBILE_FOUR:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.WEB_MOBILE_THREE:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.WEB_MOBILE_TWO:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.WEB_UNICOM_FOUR:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.WEB_UNICOM_THREE:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.WEB_UNICOM_TWO:
                            Thread.sleep(5000);
                            break;
                        case ConstantData.WEB_TELCOM_FOUR:
                            Thread.sleep(3000);
                            break;
                        case ConstantData.WEB_TELCOM_TWO:
                            Thread.sleep(5000);
                            break;

                    }

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 启动线程
     * */
    private void start() {
        if (thread == null) {
            thread = new MyThread();
            thread.start();
        }
    }

    /**
     * 停止线程
     * */
    private void stop() {
        if (thread != null) {
            thread.stop = true;
            thread = null;
        }
    }
    void send_bd_msg(int web) {
        String str = "";
        //Log.i(TAG,Integer.toString(count));
        switch (web){
            case ConstantData.WEB_LTE:
                if(LTE_COUNT == -1){
                    str = "AT^MODECONFIG=38\r";
                    deal_handler(str);
                    LTE_COUNT++;
                }else if(LTE_COUNT % 6 == 0){
                    str = "AT+LOCKPLMN=1,\"46000\",3\r";//中国移动4g
                    deal_handler(str);
                    LTE_COUNT++;
                }else if(LTE_COUNT % 6 == 1){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    LTE_COUNT++;
                }else if(LTE_COUNT % 6 == 2){
                    str = "AT+LOCKPLMN=1,\"46001\",3\r";//中国联通4g
                    deal_handler(str);
                    LTE_COUNT++;
                }else if(LTE_COUNT % 6 == 3){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    LTE_COUNT++;
                }else if (LTE_COUNT % 6 == 4){
                    str = "AT+LOCKPLMN=1,\"46011\",3\r";//中国电信4g
                    deal_handler(str);
                    LTE_COUNT++;
                }else if(LTE_COUNT % 6 == 5) {
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    LTE_COUNT++;
                }
                break;
            case ConstantData.WEB_GSM:
                if(GSM_COUNT % 7 == 0){
                    str = "AT^MODECONFIG=13\r";
                    deal_handler(str);
                    GSM_COUNT++;
                }else if(GSM_COUNT % 7 == 1){
                    str = "AT+LOCKPLMN=1,\"46000\",0\r";//中国移动2g
                    deal_handler(str);
                    GSM_COUNT++;
                }else if(GSM_COUNT % 7 == 2){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    GSM_COUNT++;
                }else if(GSM_COUNT % 7 == 3){
                    str = "AT+LOCKPLMN=1,\"46001\",0\r";//中国联通2g
                    deal_handler(str);
                    GSM_COUNT++;
                }else if(GSM_COUNT % 7 == 4){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    GSM_COUNT++;
                }else if(GSM_COUNT % 7 == 5){
                    str = "AT^MODECONFIG=22\r";//电信2g
                    deal_handler(str);
                    GSM_COUNT++;
                }else if(GSM_COUNT % 7 == 6){
                    str = "AT+BSINFO\r";
                    deal_handler(str);
                    GSM_COUNT++;
                }
                break;
            case ConstantData.WEB_MOBILE:
                if(MOBILE_COUNT % 8 == 0){
                    str = "AT^MODECONFIG=38\r";
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 1){
                    str = "AT+LOCKPLMN=1,\"46000\",3\r";//中国移动4g
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 2){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 3){
                    str = "AT^MODECONFIG=15\r";//移动3g
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 4){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 5){
                    str = "AT^MODECONFIG=13\r";
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 6){
                    str = "AT+LOCKPLMN=1,\"46000\",0\r";//中国移动2g
                    deal_handler(str);
                    MOBILE_COUNT++;
                }else if(MOBILE_COUNT % 8 == 7){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    MOBILE_COUNT++;
                }
                break;
            case ConstantData.WEB_UNICOM:
                if(UNICOM_COUNT % 8 == 0){
                    str = "AT^MODECONFIG=38\r";
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 1){
                    str = "AT+LOCKPLMN=1,\"46001\",3\r";//中国联通4g
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 2){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 3){
                    str = "AT^MODECONFIG=14\r";//联通3g
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 4){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 5){
                    str = "AT^MODECONFIG=13\r";
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 6){
                    str = "AT+LOCKPLMN=1,\"46001\",0\r";//中国联通2g
                    deal_handler(str);
                    UNICOM_COUNT++;
                }else if(UNICOM_COUNT % 8 == 7){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    UNICOM_COUNT++;
                }
                break;
            case ConstantData.WEB_TELECOM:
                if(TELECOM_COUNT % 5 == 0){
                    str = "AT^MODECONFIG=38\r";
                    deal_handler(str);
                    TELECOM_COUNT++;
                }else if(TELECOM_COUNT % 5 == 1){
                    str = "AT+LOCKPLMN=1,\"46011\",3\r";//中国电信4g
                    deal_handler(str);
                    TELECOM_COUNT++;
                }else if(TELECOM_COUNT % 5 == 2){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    TELECOM_COUNT++;
                }else if(TELECOM_COUNT % 5 == 3){
                    str = "AT^MODECONFIG=22\r";//电信2g
                    deal_handler(str);
                    TELECOM_COUNT++;
                }else if(TELECOM_COUNT % 5 == 4){
                    str = "AT+BSINFO\r";
                    deal_handler(str);
                    TELECOM_COUNT++;
                }
                break;
            case ConstantData.WEB_MOBILE_FOUR:
                if(WEB_FOUR_TWO_COUNT == -2){
                    str = "AT^MODECONFIG=38\r";
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else if(WEB_FOUR_TWO_COUNT == -1){
                    str = "AT+LOCKPLMN=1,\"46000\",3\r";//中国移动4g
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }
                break;
            case ConstantData.WEB_MOBILE_THREE:
                if(WEB_THREE_TELTWO_COUNT == -1){
                    str = "AT^MODECONFIG=15\r";//移动3g
                    deal_handler(str);
                    WEB_THREE_TELTWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_COUNT++;
                }
                break;
            case ConstantData.WEB_MOBILE_TWO:
                if(WEB_FOUR_TWO_COUNT == -2){
                    str = "AT^MODECONFIG=13\r";//2g
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else if(WEB_FOUR_TWO_COUNT == -1){
                    str = "AT+LOCKPLMN=1,\"46000\",0\r";//中国移动
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }
                break;
            case ConstantData.WEB_UNICOM_FOUR:
                if(WEB_FOUR_TWO_COUNT == -2){
                    str = "AT^MODECONFIG=38\r";//4g
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else if(WEB_FOUR_TWO_COUNT == -1){
                    str = "AT+LOCKPLMN=1,\"46001\",3\r";//中国联通
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }
                break;
            case ConstantData.WEB_UNICOM_THREE:
                if(WEB_THREE_TELTWO_COUNT == -1){
                    str = "AT^MODECONFIG=14\r";//联通3g
                    deal_handler(str);
                    WEB_THREE_TELTWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_COUNT++;
                }
                break;
            case ConstantData.WEB_UNICOM_TWO:
                if(WEB_FOUR_TWO_COUNT == -2){
                    str = "AT^MODECONFIG=13\r";//2g
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else if(WEB_FOUR_TWO_COUNT == -1){
                    str = "AT+LOCKPLMN=1,\"46001\",0\r";//中国联通
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }
                break;
            case ConstantData.WEB_TELCOM_FOUR:
                if(WEB_FOUR_TWO_COUNT == -2){
                    str = "AT^MODECONFIG=38\r";//4g
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else if(WEB_FOUR_TWO_COUNT == -1){
                    str = "AT+LOCKPLMN=1,\"46011\",3\r";//中国电信
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }else{
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_FOUR_TWO_COUNT++;
                }
                break;
            case ConstantData.WEB_TELCOM_TWO:
                if(WEB_THREE_TELTWO_COUNT == -1){
                    str = "AT^MODECONFIG=22\r";//电信2g
                    deal_handler(str);
                    WEB_THREE_TELTWO_COUNT++;
                }else{
                    str = "AT+BSINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_COUNT++;
                }
                break;
            case ConstantData.MOBILE_FOUR_NEI:
                if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -2){
                    str = "AT^MODECONFIG=38\r";         //设置4G
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -1) {
                    str = "AT+LOCKPLMN=1,\"46000\",3\r";//中国移动
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }
                break;
            case ConstantData.MOBILE_THREE_NEI:
                if(WEB_THREE_TELTWO_NEI_COUNT ==-1){
                    str = "AT^MODECONFIG=15\r";//移动3g
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }else if(WEB_THREE_TELTWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }else if(WEB_THREE_TELTWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }
                break;
            case ConstantData.MOBILE_TWO_NEI:
                if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -2){
                    str = "AT^MODECONFIG=13\r";         //设置2G
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -1) {
                    str = "AT+LOCKPLMN=1,\"46000\",0\r";//中国移动
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }
                break;
            case ConstantData.UNICOM_FOUR_NEI:
                if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -2){
                    str = "AT^MODECONFIG=38\r";         //设置4G
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -1) {
                    str = "AT+LOCKPLMN=1,\"46001\",3\r";//中国联通
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }
                break;
            case ConstantData.UNICOM_THREE_NEI:
                if(WEB_THREE_TELTWO_NEI_COUNT ==-1){
                    str = "AT^MODECONFIG=14\r";//联通3g
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }else if(WEB_THREE_TELTWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }else if(WEB_THREE_TELTWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }
                break;
            case ConstantData.UNICOM_TWO_NEI:
                if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -2){
                    str = "AT^MODECONFIG=13\r";         //设置2G
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -1) {
                    str = "AT+LOCKPLMN=1,\"46001\",0\r";//中国联通
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }
                break;
            case ConstantData.TELCOM_FOUR_NEI:
                if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -2){
                    str = "AT^MODECONFIG=38\r";         //设置4G
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT == -1) {
                    str = "AT+LOCKPLMN=1,\"46011\",3\r";//中国电信
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 0){
                    str = "AT+SCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }else if(WEN_MOBILE_FOUR_TWO_NEI_COUNT % 2 == 1){
                    str = "AT+NCELLINFO\r";
                    deal_handler(str);
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT++;
                }
                break;
            case ConstantData.TELCOM_TWO_NEI:
                if(WEB_THREE_TELTWO_NEI_COUNT ==-1){
                    str = "AT^MODECONFIG=22\r";//电信2g
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }else{
                    str = "AT+BSINFO\r";
                    deal_handler(str);
                    WEB_THREE_TELTWO_NEI_COUNT++;
                }
                break;
            default:
                break;
        }
        /*if(count == -1){
            str = "AT^MODECONFIG=15\r";
            deal_handler(str);
        }else{
            str = "AT+SCELLINFO\r";
            deal_handler(str);
        }*/
        //count++;
        byte[] buf = new byte[str.length()];
        buf = str.getBytes();
        sendMessage(buf,str.length());
    }
    private void sendMessage(byte message[], int len) {
        if (len > 0) {
            byte[] buf = new byte[len];
            System.arraycopy(message,0,buf,0,len);
            Ble_Activity.mChatService.write(buf);
            Log.i(TAG, "send msg:" + Integer.toHexString((buf[0]&0xff))+Integer.toHexString((buf[1]&0xff))+Integer.toHexString((buf[2]&0xff)));
        }

    }
    void deal_handler(String data){
        Message msg = mHandler.obtainMessage(ConstantData.MESSAGE_DEAL_UI);
        Bundle bundle = new Bundle();
        bundle.putString("DEAL_UI",data);//"Can not connect to the bluetooth");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /*地图*/

    private String amapLocationToString(AMapLocation location) {
        StringBuffer locString = new StringBuffer();
        locString.append(location.getLatitude()).append(",");
        locString.append(location.getLongitude()).append(",");
        locString.append(location.getProvider()).append(",");
        locString.append(location.getTime()).append(",");
        locString.append(location.getSpeed()).append(",");
        locString.append(location.getBearing());
        return locString.toString();
    }
    private void initpolyline() {
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(23);
        mPolyoptions.color(Color.BLUE);
    }
    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        mAMap.setLocationSource(this);// 设置定位监听
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        unregisterBroadcastReceiver();
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        startlocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();

        }
        mLocationClient = null;
    }
    /**
     * 定位结果回调
     * @param amapLocation 位置信息类
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                LatLng mylocation = new LatLng(amapLocation.getLatitude(),
                        amapLocation.getLongitude());
                mAMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                if (drawMap) {
                    mPolyoptions.add(mylocation);
                    String strlat = Double.toString(amapLocation.getLatitude());
                    String strlon = Double.toString(amapLocation.getLongitude());
                    mlat.setText(strlat);
                    mlon.setText(strlon);
                    tempdata.put("strlat",strlat);
                    tempdata.put("strlng",strlon);
                    is_loc_data = true;
                    if(tempdata.containsKey("strlac")){
                        Data data = new Data();
                        data.lat = tempdata.get("strlat").toString();
                        data.lng = tempdata.get("strlng").toString();
                        data.lac = tempdata.get("strlac").toString();
                        data.cid = tempdata.get("strcid").toString();
                        data.bid = tempdata.get("strbid").toString();
                        data.mnc = tempdata.get("strmnc").toString();
                        data.mcc = tempdata.get("strmcc").toString();
                        data.time = tempdata.get("strtime").toString();
                        data.cellMode = tempdata.get("cellMode").toString();
                        data.channel = tempdata.get("strchannel").toString();
                        data.rxlevel = tempdata.get("strrxlevel").toString();
                        userDao = new UserDao(this);
                        userDao.insertData(data);
                        //s.add(data);
                        tempdata.clear();
                        is_loc_data = false;
                    }


                    redrawline();
                }
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": "
                        + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 开始定位。
     */
    private void startlocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mLocationClient.setLocationListener(this);
            // 设置为Device_Sensors
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);

            mLocationOption.setInterval(2000);

            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();

        }
    }

    /**
     * 实时轨迹画线
     */
    private void redrawline() {
        if (mPolyoptions.getPoints().size() > 1) {
            if (mpolyline != null) {
                mpolyline.setPoints(mPolyoptions.getPoints());
            } else {
                mpolyline = mAMap.addPolyline(mPolyoptions);
            }
        }
    }
    /*class SpinnerWebSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Log.v(TAG, "onItemSelected"+arg2);
            //keyboardIsOpen = inputMgr.isActive(); //如果输入软键盘打开了，则关闭软键盘
            *//*if (keyboardIsOpen)// imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS); //没有显示则显示
                inputMgr.hideSoftInputFromWindow(ed_gsm_freq.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);*//*
            switch (arg2) {
                case 0:
                    break;
                case 1:
                    web = 1;
                    break;
                case 2:
                    web = 2;
                    break;
                case 3:
                    web = 3;
                    break;
                case 4:
                    web = 4;
                    break;
                case 5:
                    web = 5;
                    break;
                default:
                    //spinnerWeb.setSelection(2, true);
                    return;
                //break;
            }

            if (!ConstantData.Flag_BtConnected)
                Toast.makeText(getApplicationContext(),R.string.Cation_Disconnected, Toast.LENGTH_SHORT).show();
            else
            {
                //spinnerWeb.setEnabled(false);
                if(is_Toast){
                    Toast.makeText(getApplicationContext(),R.string.Cation_SwitchNetWork, Toast.LENGTH_SHORT).show();
                }
                if (btn.isChecked()) {
                    stop();
                    LTE_COUNT = -1;
                    GSM_COUNT = -1;
                    MOBILE_COUNT = 0;
                    UNICOM_COUNT = 0;
                    TELECOM_COUNT = 0;
                    start();
                }

            }
        }*/
        void registerBroadcastReceiver() {
            if (mBroadcastReciver == null) {
                mBroadcastReciver   = new MyBroadcastReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConstantData.ACTION_LOAD_SOUND_FINISH);
                registerReceiver(mBroadcastReciver, filter);
            }
        }

        void unregisterBroadcastReceiver() {
            if (mBroadcastReciver != null) {
                unregisterReceiver(mBroadcastReciver);
                mBroadcastReciver  = null;
            }
        }

        private class MyBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ConstantData.ACTION_LOAD_SOUND_FINISH)) {
                    //invalidateOptionsMenu();

                }
            }
        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            Log.v(TAG, "onCreateOptionsMenu" );
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
    @Override
    public boolean
    onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id)
        {
            case R.id.start:
                //开始
                mAMap.clear(true);
                if (!ConstantData.Flag_BtConnected)
                    Toast.makeText(getApplicationContext(),R.string.nonconnect, Toast.LENGTH_SHORT).show();
                else{
                    is_Toast = true;
                    is_Start = true;
                    drawMap = true;
                    start();
                }
                return true;
            case R.id.finish:
                //结束
                if(is_Start){
                    is_Start = false;
                    drawMap = false;
                    stop();
                    LTE_COUNT = -1;
                    GSM_COUNT = 0;
                    MOBILE_COUNT = 0;
                    UNICOM_COUNT = 0;
                    TELECOM_COUNT = 0;
                    WEN_MOBILE_FOUR_TWO_NEI_COUNT = -2;
                    WEB_THREE_TELTWO_NEI_COUNT = -1;
                    WEB_FOUR_TWO_COUNT = -2;
                    WEB_THREE_TELTWO_COUNT = -1;
                    db = SQLiteDatabase.openOrCreateDatabase(this.getFilesDir()+"/my.db3",null);
                    /*while (locqueue!=null){
                        String title = locqueue.poll();
                        String content = Ble_Activity.blequeue.poll();

                        *//*try{
                            //db.execSQL("insert into news_inf values(null,?,?)",new String[]{title,content});

                        }catch(SQLiteException se){
                            Log.i(TAG,"woshishuaige");
                            db.execSQL("create table news_inf(_id integer primary key autoincrement,"
                                    +"news_title varchar(50) ,"
                                      +"news_content varchar(255))");
                            db.execSQL("insert into news_inf values(null,?,?)",new String[]{title,content});
                        }*//*
                    }*/

                }else{
                    Toast.makeText(getApplicationContext(),R.string.nonstart,Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.pause:
                if(is_Start){
                    is_Pause_Continue = false;
                }else{
                    Toast.makeText(getApplicationContext(),R.string.nonstart,Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.con:
                if(is_Start){
                    is_Pause_Continue = true;
                }else{
                    Toast.makeText(getApplicationContext(),R.string.nonstart,Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
