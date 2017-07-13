package com.example.zhuoh.zheng_map_ble_z;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "Main";
    /*Button*/
    public static Button btn_zheng_bl_connect;
    public static Button btn_zheng_map_content;

    // 成员对象的聊天服务

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_ui();
    }

    void init_ui() {
        btn_zheng_bl_connect = (Button) findViewById(R.id.btn_lochelper);
        btn_zheng_bl_connect.setText("定位助手");
        btn_zheng_map_content = (Button) findViewById(R.id.btn_roadtest);
        btn_zheng_map_content.setText("路测采集");
        btn_zheng_bl_connect.setOnClickListener(this);
        btn_zheng_map_content.setOnClickListener(this);
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
            case R.id.btn_lochelper:
                Intent intent_Ble = new Intent(MainActivity.this, Ble_Activity.class);
                startActivity(intent_Ble);
                //startActivityForResult(intent_Ble, ConstantData.REQUEST_CONNECT_DEVICE);
                break;
            case R.id.btn_roadtest:
                Intent intent_Map = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent_Map);
                break;
            default:
        }
    }
}


