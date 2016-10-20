package com.example.achuan.materialdesign_study.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.achuan.materialdesign_study.app.MyApplication;

/**
 * Created by achuan on 16-9-10.
 * 功能：存储设置及一些全局的信息到SharedPreferences文件中
 */
public class SharedPreferenceUtil {
    //设置默认模式
    //private static final boolean DEFAULT_AUTO_OPEN_BlueTooth = false;//默认蓝牙不自动打开
    //创建的SharedPreferences文件的文件名
    private static final String SHAREDPREFERENCES_NAME = "my_sp";

    //1-创建一个SharedPreferences文件
    public static  SharedPreferences getAppSp() {
        return MyApplication.getInstance().getSharedPreferences(
                SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    /****2-定义get和set方法,实现对SharedPreferences文件中属性值的读取和修改****/
    //获取模式的方法
    /*public static boolean getAutoOpenBlueToothState() {
        return getAppSp().getBoolean(Constants.SP_AUTO_OPEN_BLUETOOTH, DEFAULT_AUTO_OPEN_BlueTooth);
    }
    //改变模式的方法
    public static void setAutoOpenBlueToothState(boolean state) {
        getAppSp().edit().putBoolean(Constants.SP_AUTO_OPEN_BLUETOOTH, state).commit();
    }*/




}
