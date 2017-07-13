package com.example.zhuoh.zheng_map_ble_z;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Vera on 2016/4/8.
 */
public class ConstantData {
    //    public static boolean D = true;
//    public static final String info = "junge";
    public static boolean Flag_BtConnected = false;
    public static boolean Flag_Syn = false;
    public static boolean Flag_Freq_Config = false;


    public static int FREQ_G;
    public static int FREQ_W;
    public static int FREQ_F;
    public static int FREQ_TDD;
    public static int PCI_TDD;
    public static int web = 1;
    //public static final int WEB_GSM   = 0;
    public static final int WEB_TD_SCDMA = 1;
    public static final int WEB_WCDMA = 2;
    public static final int WEB_CDMA = 3;
    public static final int WEB_TDD = 4;
    public static final int WEB_FDD = 5;
    public static final int WEB_LTE = 1;//表示4g混合
    public static final int WEB_GSM = 2;//表示2g混合
    public static final int WEB_MOBILE = 3;//表示移动混合
    public static final int WEB_UNICOM = 4;//表示联通混合
    public static final int WEB_TELECOM = 5;//表示电信混合
    public static final int WEB_MOBILE_FOUR = 6;//表示移动4G
    public static final int WEB_MOBILE_THREE = 7;//表示移动3G
    public static final int WEB_MOBILE_TWO = 8;//表示移动2G
    public static final int WEB_UNICOM_FOUR = 9;//表示联通4G
    public static final int WEB_UNICOM_THREE = 10;//表示联通3G
    public static final int WEB_UNICOM_TWO = 11;//表示联通2G
    public static final int WEB_TELCOM_FOUR = 12;//表示电信4G
    public static final int WEB_TELCOM_TWO = 13;//表示电信2G
    public static final int MOBILE_FOUR_NEI = 14;//表示移动4G邻区
    public static final int MOBILE_THREE_NEI = 15;//表示移动3G邻区
    public static final int MOBILE_TWO_NEI = 16;//表示移动2G邻区
    public static final int UNICOM_FOUR_NEI = 17;//表示联通4G邻区
    public static final int UNICOM_THREE_NEI = 18;//表示联通3G邻区
    public static final int UNICOM_TWO_NEI = 19;//表示联通2G邻区
    public static final int TELCOM_FOUR_NEI = 20;//表示电信4G邻区
    public static final int TELCOM_TWO_NEI = 21;//表示电信2G邻区
    public static int Freq = 0;//发给单兵的频率


    // 类型的消息发送从bluetoothchatservice处理程序
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    public static final int MESSAGE_BATTERY = 6;
    public static final int MESSAGE_XINHAO = 7;
    public static final int MESSAGE_QITA = 8;
    public static final int MESSAGE_HEARTBEAT = 9;
    public static final int MESSAGE_DEBUGINFO = 10;
    public static final int MESSAGE_RECONFIG = 11;

    public static final int MESSAGE_TDDSTRTH = 12;
    public static final int MESSAGE_SUCCESS = 13;
    public static final int MESSAGE_DEAL_UI = 14;


    // 键名字从收到的bluetoothchatservice处理程序
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADD = "device_add";
    public static final String TOAST = "toast";
    // 独特的是这个应用程序

    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Intent需要 编码
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static int HONGKONG = 0;

    public static final String[] web_array = {"2G-GSM", "4G-LTE", "MOBILE", "UNICOM", "TELECOM"};


    /*Action*/
    public static final String ACTION_LOAD_SOUND_FINISH = "com.xy.vera.mbdn.sound.load.finish";

    public static final int TIMER_STATE_WAITACK = 0;
    public static final int TIMEOUT_WAITACK = 5000; /*1.5秒*/
    static ArrayList<Data> s = new ArrayList<Data>();

}

class Data{
    public String lat;
    public String lng;
    public String lac;
    public String cid;
    public String bid;
    public String time;
    public String cellMode;
    public String mcc;
    public String mnc;
    public String channel;
    public String rxlevel;
}
