package com.example.zhuoh.zheng_map_ble_z;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by zhuo on 2017/6/26.
 */

public class HytcSQLiteOpenHelper extends SQLiteOpenHelper {
    //数据库的名称
    private static String name="hytc.db";
    //数据库的版本
    private static Integer version=1;

    /**
     * 只需保证 在实例化子类对象时，保证父类对象先实例化
     * @param context
     */
    public HytcSQLiteOpenHelper(Context context) {
        super(context, name, null, version);
    }

    /**
     * 第一次执行的时候创建数据库
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE basedata (id integer primary key autoincrement, time varchar(20),lat varchar(20),lng varchar(20)," +
                "lac varchar(20),cid varchar(20),bid varchar(20),mcc varchar(20),mnc varchar(20),cellMode varchar(20),channel varchar(20),rxlevel varchar(20))");
    }

    /**
     * 当版本号发生变化的时候 触发
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("alter table queryinfo add column phone varchar(11)");

    }
}
