package com.example.achuan.materialdesign_study.model.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.achuan.materialdesign_study.model.bean.WXItemBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by achuan on 16-10-3.
 * 功能：网络请求及相关数据处理的方法
 */
public class HttpUtil {
    //private static WechatAsyncTask mWechatAsyncTask;

    /*****4-包装一个方法,可以开启AsyncTask任务获取数据,并可以根据传入的参数进行不同的处理*****//*
    public static List<WXItemBean> startWechatAsyncTaskForBean(String httpArg )
    {
        mWechatAsyncTask=new WechatAsyncTask();
        mWechatAsyncTask.execute(Constants.URL,httpArg);
        return null;
    }*/

    /***3-使用HttpURLConnection通过url链接获取网络图片***/
    public static Bitmap getBitmapFromURL(String urlString)
    {
        Bitmap bitmap=null;
        HttpURLConnection connection = null;
        InputStream inputStream = null;//输入流的引用变量
        try {
            URL url=new URL(urlString);
            connection= (HttpURLConnection) url.openConnection();//打开网络连接
            //将简单的字节流包装成缓存流
            inputStream=new BufferedInputStream(connection.getInputStream());
            /*将输入流解析成bitmp类型的数据*/
            bitmap= BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭网络连接
            if(connection!=null) {
                connection.disconnect();
            }
            //最后记得关闭输入流
            if(inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /***2-对传入的链接进行流读取,并JSON解析后获取最终数据***/
    public static List<WXItemBean> getJsonData(String httpUrl, String httpArg)  {
        List<WXItemBean> WXItemBeanList;//声明一个引用变量
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();//存储服务器返回数据
        try {
            URL url = new URL(httpUrl+ "?"+ httpArg);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  "a849abd2d158cff676785c63f250bfd3");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //引用变量指向解析出来的实例对象
        WXItemBeanList=HttpUtil.parseJsonWithJSONObject(sbf.toString());
        return WXItemBeanList;
    }

    /***１－JSON数据解析：使用JSONObject***/
    public static List<WXItemBean> parseJsonWithJSONObject(String jsonData) {
        List<WXItemBean> WXItemBeanList=new ArrayList<>();//定义一个集合来存储解析出来的每组数据
        WXItemBean WXItemBean;
        try{
            JSONObject jsonObject;
            jsonObject=new JSONObject(jsonData);//先获取整个JSON数据对象
            JSONArray jsonArray=jsonObject.getJSONArray("newslist");//获取"newslist"部分的集合
            //循环遍历数组,取出每个元素,每个元素都是一个JSONObject对象,并取出元素对应的数据信息
            for (int i = 0; i <jsonArray.length() ; i++) {
                jsonObject=jsonArray.getJSONObject(i);
                String newsctime  =jsonObject.getString("ctime");
                String newstitle  =jsonObject.getString("title");
                String newsdescription=jsonObject.getString("description");
                String newspicUrl =jsonObject.getString("picUrl");
                String newsurl    =jsonObject.getString("url");
                //创建单组数据的实例对象
                WXItemBean=new WXItemBean(newsctime,newstitle,newsdescription,newspicUrl,newsurl);
                WXItemBeanList.add(WXItemBean);//存储实例到集合中
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return WXItemBeanList;
    }

}
