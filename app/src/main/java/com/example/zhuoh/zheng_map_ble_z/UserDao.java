package com.example.zhuoh.zheng_map_ble_z;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuo on 2017/6/26.
 */

public class UserDao {
    // 数据库管理操作对象
    private HytcSQLiteOpenHelper helper;
    private List<Map<String, Object>> data;
    private int pagesize = 12;

    public UserDao(Context context){
        helper = new HytcSQLiteOpenHelper(context);
    }

    public UserDao(List<Map<String, Object>> data, Context context) {
        this.data = data;
        helper = new HytcSQLiteOpenHelper(context);
    }

    /**
     * 查询方法
     */

    public void getObjects(Integer nowpage) {
        // 获取SQLiteDatabase对象
        SQLiteDatabase db = helper.getReadableDatabase();
        // 计算开始的记录数
        int startSize = (nowpage - 1) * pagesize;
        Cursor c = db.rawQuery("select id,lac,cid,bid,mode from queryinfo limit ?,?",
                new String[] { startSize + "", pagesize + "" });

        while (c.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("lac", c.getString(c.getColumnIndex("lac")));
            map.put("cid", c.getString(c.getColumnIndex("cid")));
            map.put("bid", c.getString(c.getColumnIndex("bid")));
            map.put("id", c.getInt(c.getColumnIndex("id")));
            map.put("mode", c.getString(c.getColumnIndex("mode")));
            data.add(map);
        }
        c.close();
        db.close();

    }

    public void getAllObjects() {
        // 获取SQLiteDatabase对象
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select id,lac,cid,bid,mode from queryinfo",null);

        while (c.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("lac", c.getString(c.getColumnIndex("lac")));
            map.put("cid", c.getString(c.getColumnIndex("cid")));
            map.put("bid", c.getString(c.getColumnIndex("bid")));
            map.put("id", c.getInt(c.getColumnIndex("id")));
            map.put("mode", c.getString(c.getColumnIndex("mode")));
            data.add(map);
        }
        c.close();
        db.close();

    }

    public Map<String ,Object> getObjectbynum(int id){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select lat,lng,lac,cid,bid,distance,result from queryinfo where id=?",new String[]{id+""});
        Map<String, Object> map = new HashMap<String, Object>();
        while (c.moveToNext()){
            map.put("lat", c.getString(c.getColumnIndex("lat")));
            map.put("lng", c.getString(c.getColumnIndex("lng")));
            map.put("lac", c.getString(c.getColumnIndex("lac")));
            map.put("cid", c.getString(c.getColumnIndex("cid")));
            map.put("bid", c.getString(c.getColumnIndex("bid")));
            map.put("distance", c.getString(c.getColumnIndex("distance")));
            map.put("result", c.getString(c.getColumnIndex("result")));
        }
        return map;
    }

    public void insertData(Data data){
        SQLiteDatabase db = helper.getReadableDatabase();
        db.execSQL("insert into basedata(time,lat, lng,lac,cid,bid,mcc,mnc,cellMode,channel,rxlevel) values(?,?,?,?,?,?,?,?,?,?,?)",new Object[]{data.time,data.lat,data.lng,data.lac,data.cid,data.bid,data.mcc,data.mnc,data.cellMode,data.channel,data.rxlevel});
        db.close();
    }

    public int getPages() {
        int pages = 0;
        // 获取SQLiteDatabase对象
        SQLiteDatabase db = helper.getReadableDatabase();
        // 计算开始的记录数
        Cursor c = db.rawQuery("select count(*) as c from queryinfo", null);
        if (c.moveToNext()) {
            int count = c.getInt(c.getColumnIndex("c"));
            //计算总页数
            pages = count % pagesize == 0 ? count / pagesize : count / pagesize
                    + 1;
        }
        c.close();
        db.close();
        return pages;
    }
}
