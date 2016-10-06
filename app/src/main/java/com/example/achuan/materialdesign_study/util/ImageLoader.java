package com.example.achuan.materialdesign_study.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.example.achuan.materialdesign_study.R;
import com.example.achuan.materialdesign_study.adapter.WXIAdapter;
import com.example.achuan.materialdesign_study.model.http.HttpUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by achuan on 16-10-4.
 * 功能：实现网络图片的加载以及(内存)缓存
 */
public class ImageLoader {

    //创建Cache引用变量
    private LruCache<String,Bitmap> mLruCache;

    private RecyclerView mRecyclerView;//通过拿到具体的列表对象来对需要加载的item进行图片加载
    private Set<NewsAsyncTask> mTask;//创建一个集合来存储所有的线程

    //从缓存中获取图片资源的方法
    public Bitmap getBitmpFromCache(String url)
    {
        //LruCache内部采用的是LinkedHashMap强引用方式存储外界的缓存对象
        return mLruCache.get(url);
    }
    //往缓存中添加图片资源的方法
    public void addBitmpToCache(String url,Bitmap bitmap)
    {
        //如果当前图片没有缓存才将其缓存进去
        if(getBitmpFromCache(url)==null)
        {
            mLruCache.put(url,bitmap);
        }
    }

    public ImageLoader(RecyclerView recyclerView) {
        mRecyclerView=recyclerView;
        mTask=new HashSet<>();

        //获取当前进程的最大可用内存
        //除以1024是为了将单位转换为：KB
        int maxMemory= (int) (Runtime.getRuntime().maxMemory()/1024);
        //设置总容量的大小
        int cacheSize=maxMemory/8;
        //初始化:创建LruCache来实现内存缓存
        mLruCache=new LruCache<String, Bitmap>(cacheSize){
            //计算缓存对象的大小 单位：KB
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getRowBytes()*value.getHeight()/1024;
            }
        };
    }

    /*****图片加载显示的优化处理方法*****/
    //取消掉所有正在执行的任务
    public void cancelAllTasks() {
        if(mTask!=null)
        {
            //变量集合中的所有任务,将它们全部取消掉
            for (NewsAsyncTask task:mTask) {
                task.cancel(false);
            }
        }
    }
    //根据列表显示界面中的范围来加载这个范围类的图片资源
    public void loadImages(int start, int end) {
        for (int i = start; i <=end ; i++) {
            String url= WXIAdapter.URLS[i];
            //从缓存中取出key值对应的图片
            Bitmap bitmap=getBitmpFromCache(url);
            //如果图片没有缓存,这时才加载网络图片,并将其缓存下来
            if(bitmap==null) {
                NewsAsyncTask task=new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);//将task任务存储起来,方便后续管理
            }
            else {
                //缓存中存在图片时直接将其显示出来即可
                //通过添加的tag来加载控件对象
                ImageView imageView= (ImageView) mRecyclerView.findViewWithTag(url);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    //使用AsyncTask机制来加载网络图片
    public void showImageByAsyncTask(ImageView imageView,String url)
    {
        //从缓存中取出key值对应的图片
        Bitmap bitmap=getBitmpFromCache(url);
        //如果图片没有缓存,这时才加载网络图片,并将其缓存下来
        if(bitmap==null) {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            //缓存中存在图片时直接将其显示出来即可
            imageView.setImageBitmap(bitmap);
        }
    }
    //AsyncTask<1,2,3> 3个参数的介绍：1传入给后台任务的数据的类型,2进度值类型,3返回值类型
    class NewsAsyncTask  extends AsyncTask<String,Void,Bitmap> {
        private String mUrl;
        public NewsAsyncTask(String url) {
            mUrl=url;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        //这里进行图片加载以及缓存的工作
        @Override
        protected Bitmap doInBackground(String... strings) {
            //如果当前任务被取消了,直接退出任务
            if(isCancelled()) return  null;
            String url=strings[0];//获取传入的网络链接
            /*****从网络上获取图片,并将该图片加入缓存*****/
            Bitmap bitmap= HttpUtil.getBitmapFromURL(url);
            //将图片保存到缓存中
            if(bitmap!=null) {
                addBitmpToCache(url,bitmap);
            }
            return bitmap;
        }
        //图片加载完后进行显示处理,显示到对应的item中
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //如果当前任务被取消了,直接退出任务
            if(isCancelled()) return;
            super.onPostExecute(bitmap);
            ImageView imageView= (ImageView) mRecyclerView.findViewWithTag(mUrl);
            if(imageView!=null&&bitmap!=null) {
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);//任务执行完后将进程从集合中移除
        }
    }
}
