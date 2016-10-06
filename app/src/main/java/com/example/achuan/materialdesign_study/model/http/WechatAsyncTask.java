package com.example.achuan.materialdesign_study.model.http;

import android.os.AsyncTask;

import com.example.achuan.materialdesign_study.model.WXItemBean;

import java.util.List;

/**
 * Created by achuan on 16-10-5.
 * 功能：实现网络请求返回JSON数据,并获取最后JSON解析后的数据集合
 */

//AsyncTask<1,2,3> 3个参数的介绍：1传入给后台任务的数据的类型,2进度值类型,3返回值类型
public class WechatAsyncTask extends AsyncTask<String,Void,List<WXItemBean>> {
    /***********
     * 定义网络数据请求返回后的接口
     *********/
    public interface HttpCallbackListener {
        void onFinish(List<WXItemBean> wxItemBeanList);
        void onError();
    }
    private  HttpCallbackListener mHttpCallbackListener;

    public  void setHttpCallbackListener(HttpCallbackListener listener) {
        this.mHttpCallbackListener = listener;
    }
    //后台任务执行前的初始化操作
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    //后台子线程执行耗时操作的方法
    @Override
    protected List<WXItemBean> doInBackground(String... strings) {
        if(isCancelled()) return null;
        return HttpUtil.getJsonData(strings[0],strings[1]);
    }
    /*//doInBackground方法中执行publishProgress()进行进度显示后执行该方法
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }*/
    //后台任务执行完后执行该方法,并传递执行后的结果进来
    @Override
    protected void onPostExecute(List<WXItemBean> newsBeens) {
        if(isCancelled()) return;
        super.onPostExecute(newsBeens);
        //注册回调方法
        if(newsBeens!=null) {
            mHttpCallbackListener.onFinish(newsBeens);
        }
        else {
            mHttpCallbackListener.onError();
        }
    }
}

