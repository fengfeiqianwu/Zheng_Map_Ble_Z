package com.example.zhuoh.zheng_map_ble_z;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Vera on 2016/4/8.
 */
public class MyAdapterBlue extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mStringArray;
    public MyAdapterBlue(Context context, String[] stringArray) {
        super(context,android.R.layout.simple_spinner_item, stringArray);
        mContext = context;
        mStringArray=stringArray;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        //修改Spinner展开后的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, parent,false);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(mStringArray[position]);
        tv.setTextSize(15);
        //tv.setTextColor(Color.WHITE);
        tv.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
        tv.setGravity(Gravity.CENTER);
        return convertView;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 修改Spinner选择后结果的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(mStringArray[position]);
        tv.setTextSize(15);
        //tv.setBackgroundResource(R.drawable.button_blue_selector);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
        //tv.setTextColor(getContext().getResources().getColor(R.color.black));
        return convertView;
    }

}
