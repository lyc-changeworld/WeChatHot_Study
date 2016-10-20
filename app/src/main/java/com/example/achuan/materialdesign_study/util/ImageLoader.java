package com.example.achuan.materialdesign_study.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.example.achuan.materialdesign_study.R;
import com.example.achuan.materialdesign_study.model.http.HttpUtil;
import com.example.achuan.materialdesign_study.ui.adapter.WXIAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by achuan on 16-10-4.
 * 功能：实现网络图片的加载以及(内存)缓存
 */
public class ImageLoader {
    private static final String TAG="lyc-changeworld";
    private Context mContext;

    //创建Cache引用变量
    private ImageResizer mImageResizer=new ImageResizer();//获取bitmap的高效加载对象
    /*1-应用缓存*/
    private LruCache<String,Bitmap> mLruCache;
    /*2-磁盘缓存*/
    private DiskLruCache mDiskLruCache;
    private static final long  DISK_CACHE_SIZE=1024*1024*50;//定义缓存区大小50MB
    private static final int DISK_CACHE_INDEX=0;//节点的位置（0代表第一个节点）
    private static final int IO_BUFFER_SIZE=1024*8;//定义输入或输出的缓存流为8KB
    private boolean mIsDiskLruCacheCreated=false;


    private RecyclerView mRecyclerView;//通过拿到具体的列表对象来对需要加载的item进行图片加载
    private Set<NewsAsyncTask> mTask;//创建一个集合来存储所有的线程


    //获取磁盘缓存地址的方法
    public File getDiskCacheDir(Context context, String uniqueName) {
        //缓存路径
        final String cachePath;
        /*当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，
        否则就调用getCacheDir()方法来获取缓存路径。前者获取到的就是 /sdcard/Android/data/
        <application package>/cache 这个路径，而后者获取到的是 /data/data/<application
        package>/cache 这个路径*/
        boolean externalStorageAvailable=Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable();
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        //内存路径+/（分隔符）+子文件夹名称
        return new File(cachePath + File.separator + uniqueName);
    }
    //网络图片的加载,通过输出流将加载到的资源引流到文件系统中
    public boolean downloadUrlToStream(String urlString,OutputStream outputStream) throws IOException {
        HttpURLConnection urlConnection=null;
        BufferedOutputStream out=null;
        BufferedInputStream in=null;
        try {
            final URL url=new URL(urlString);
            urlConnection= (HttpURLConnection) url.openConnection();//创建连接
            //BufferedInputStream是带缓冲区的输入流，默认缓冲区大小是8M
            // 能够减少访问磁盘的次数，提高文件读取性能
            // 指定文件带缓冲区的读取流且指定缓冲区大小为8KB
            //通过输入流将网络资源引入
            in=new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            //创建一个输出流,指向文件系统
            out=new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);
            int b;//读取标志
            //字节读取方式进行资源的引入
            while ((b=in.read())!=-1){
                out.write(b);
            }
            return  true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(urlConnection!=null){
                urlConnection.disconnect();//断开网络连接
                in.close();
                out.close();
            }
        }
        return false;
    }
    //获取磁盘缓存分区的剩余可用空间
    private long getUsableSpace(File path){
        //如果当前系统的SDK LEVEL 高于 Android 2.3的版本
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.GINGERBREAD){
            return  path.getUsableSpace();//返回该分区上的可用的字节数
        }
        //如果低于2.3的版本,则使用下面的方法获取
        final StatFs statFs=new StatFs(path.getPath());
        //单个block的大小 * 可用的block的数目
        return statFs.getBlockSizeLong()*statFs.getAvailableBlocksLong();
    }

    /***********2.DiskLruCache的缓存添加***********/
    /**下面的两个方法将实现通过图片url转换得到key**/
    //1-将字节数组转换成16进制字符串
    private String bytesToHexString(byte[] bytes){
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i <bytes.length ; i++) {
            String hex=Integer.toHexString(0xFF&bytes[i]);//将字节转换成16进制字符串
            if(hex.length()==1){
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    //2-因为图片的url中可能有特殊字符(影响直接使用),所以需要先获取图片url对应的key
    private String hashKeyFormUrl(String url){
        String cacheKey = null;
        //采用url的MD5值作为key
        try {
            final MessageDigest messageDigest;
            // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            messageDigest = MessageDigest.getInstance("MD5");
            // 输入字符串转换得到字节数组存储给对象变量
            messageDigest.update(url.getBytes());
            // 转换并返回结果,也是字节数组,包含16个元素
            // 字符数组转换成字符串
            cacheKey=bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return cacheKey;
    }

    /*********************下面的方法是实现对磁盘缓存DiskLruCache的应用*******************/
    /*3-网络图片加载,并保存到磁盘中*/
    private Bitmap loadBitmapFromHttp(String url,int reqWidth,int reqHeight){
        //网络加载的线程必须在子线程中进行,否则将终止程序
        if(Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if(mDiskLruCache==null) return null;
        //3-网络图片的加载和磁盘的缓存操作
        try {
            String key=hashKeyFormUrl(url);//通过url拿到key
            DiskLruCache.Editor editor=mDiskLruCache.edit(key);//通过key创建对应的editor对象
            if(editor!=null){
                //打开一个文件输出流,后续将通过网络加载将资源通过这个流写入到文件系统上
                OutputStream outputStream=editor.newOutputStream(DISK_CACHE_INDEX);//数据位从0开始
                if(downloadUrlToStream(url,outputStream)){
                    editor.commit();//加载成功就可以提交写入操作了
                }else {
                    editor.abort();//加载失败将回退整个editor操作
                }
                mDiskLruCache.flush();//将内存中的操作记录同步到日志文件（也就是journal文件）当中
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //磁盘存储成功后,加载返回对应的图片资源
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }
    /***3.DiskLruCache的缓存查找***/
    /*从磁盘缓存中获取图片资源
    同时将资源添加到应用缓存中*/
    private Bitmap loadBitmapFromDiskCache(String url,int reqWidth,int reqHeigth){
        //线程调试,看加载过程是不是在主线程中进行
        if(Looper.myLooper()==Looper.getMainLooper()){
            LogUtil.w(TAG,"load bitmap from UI Thread," +
                    "it's not recommended!");
        }
        if(mDiskLruCache==null) return null;
        Bitmap mBitmap=null;
        String key=hashKeyFormUrl(url);//获取key值
        try {
            DiskLruCache.Snapshot mSnapshot=mDiskLruCache.get(key);//通过key值获得对应的节点对象
            if(mSnapshot!=null){
                //获取缓存文件的输入流
                FileInputStream fileInputStream= (FileInputStream)
                        mSnapshot.getInputStream(DISK_CACHE_INDEX);
                //FileInputStream无法便捷地进行压缩,所以通过FileDescriptor来加载压缩后的图片
                //通过文件流得到所对应的文件描述符
                FileDescriptor fileDescriptor=fileInputStream.getFD();
                //通过文件描述符来创建一个流来加载一张缩放后的图片（消除不同平台下的差异）
                mBitmap =mImageResizer.decodeSampledBitmapFromFileDescriptor(
                        fileDescriptor,reqWidth,reqHeigth);
                //获取磁盘资源后将其添加到应用缓存中,可以直接使用
                if(mBitmap!=null){
                    addBitmpToCache(key,mBitmap);//添加资源到应用缓存中
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return mBitmap;
    }

    /********************下面的三个方法是对应用内存LruCache的应用***********************/
    //通过图片链接获取：应用内存中的图片资源
    private Bitmap loadBitmapFromLruCache(String url){
        final String key=hashKeyFormUrl(url);//解析出key值
        Bitmap bitmap=getBitmpFromCache(key);//通过key值获取图片资源
        return bitmap;
    }
    //通过key从应用缓存中获取图片资源的方法
    public Bitmap getBitmpFromCache(String key)
    {
        //LruCache内部采用的是LinkedHashMap强引用方式存储外界的缓存对象
        return mLruCache.get(key);
    }
    //往应用缓存中添加图片资源的方法
    public void addBitmpToCache(String key,Bitmap bitmap)
    {
        //如果当前图片没有缓存才将其缓存进去
        if(getBitmpFromCache(key)==null)
        {
            mLruCache.put(key,bitmap);
        }
    }


    /***
     * 图片的同步加载方法（图片的加载顺序控制：1应用内存—>2磁盘缓存->3网络加载->4链接流加载）
     * ***/
    public Bitmap loadBitmap(String uri,int reqWidth,int reqHeight){
        //1先去应用内存中加载
        Bitmap bitmap=loadBitmapFromLruCache(uri);
        if(bitmap!=null){
            return bitmap;
        }else {
            //否则,2接着去磁盘缓存中加载
            bitmap=loadBitmapFromDiskCache(uri,reqWidth,reqHeight);
            if(bitmap!=null){
                return bitmap;
            }else {
                //否则,3接着去网络加载获取图片
                bitmap=loadBitmapFromHttp(uri,reqWidth,reqHeight);
            }
        }
        //有一种特殊的情况：当磁盘的空间不够,无法使用磁盘缓存时,将直接通过链接流加载网络图片
        if(bitmap==null&&!mIsDiskLruCacheCreated){
            LogUtil.w(TAG,"DiskLruCache is not created.");
            bitmap=HttpUtil.getBitmapFromURL(uri);//4直接通过url链接获取网络图片
            //并将资源添加到应用缓存中
            addBitmpToCache(hashKeyFormUrl(uri),bitmap);
        }
        return bitmap;
    }

    //创建一个图片加载的实例对象
    public static ImageLoader build(Context context,RecyclerView recyclerView){
        return new ImageLoader(context,recyclerView);
    }
    public ImageLoader(Context context,RecyclerView recyclerView) {
        mContext=context.getApplicationContext();//获得整个应用的context
        mRecyclerView=recyclerView;
        mTask=new HashSet<>();
        /***1-初始化LruCache实现应用内存缓存***/
        //获取当前进程的最大可用内存
        //除以1024是为了将单位转换为：KB
        int maxMemory= (int) (Runtime.getRuntime().maxMemory()/1024);
        //设置总容量的大小
        int cacheSize=maxMemory/4;//该缓存区的大小为当前进程的可用内存的1/4
        //初始化:创建LruCache来实现内存缓存
        mLruCache=new LruCache<String, Bitmap>(cacheSize){
            //计算缓存对象的大小 单位：KB
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候调用
                return value.getRowBytes()*value.getHeight()/1024;
            }
        };
        /***2-初始化DiskLruCache实现应用磁盘缓存***/
        //设定硬盘缓存的路径（缓存路径+/+子文件夹名称）
        File diskCacheDir=getDiskCacheDir(mContext,"bitmap");
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdir();//建立一个新的子目录
        }
        /***
         * 参数介绍:1.磁盘缓存的存储路径 应用的缓存路径：/sdcard/Android/data/应用的包名/cache
         *       （如果希望卸载应用时删除缓存文件,那就将其设置在SD卡上应用的路径下）
         *        2.应用的版本号（一般设置为1即可）
         *        3.表示单个节点对应的数据的个数（一般设置为1即可）
         *        4.表示缓存的总大小  1024*1024*（xxMB）
         * ***/
        //只有在当前缓存路径分区下的可用空间足够时才创建
        if(getUsableSpace(diskCacheDir)>DISK_CACHE_SIZE){//50MB
            mIsDiskLruCacheCreated=true;
            //DiskLruCache的创建
            try {
                mDiskLruCache=mDiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*****图片加载显示的优化处理方法*****/
    //取消掉所有正在执行的任务
    public void cancelAllTasks() {
        if(mTask!=null) {
            //变量集合中的所有任务,将它们全部取消掉
            for (NewsAsyncTask task:mTask) {
                task.cancel(false);
            }
        }
    }

    /****
     *
     * 建议该方法中只进行图片的加载和缓存(第一次缓存完后可以显示一次)工作
     *
     * ****/
    //根据列表显示界面中的范围来加载这个范围类的图片资源
    public void loadImages(int start, int end) {
        NewsAsyncTask task;//引用变量
        for (int i = start; i <=end ; i++) {
            String url= WXIAdapter.URLS.get(i);
            //从应用缓存中取出url值对应的图片
            //Bitmap bitmap=getBitmpFromCache(url);
            Bitmap bitmap=loadBitmapFromLruCache(url);
            //如果图片没有缓存,这时才磁盘或者网络上加载图片,并将其存储到应用缓存中
            if(bitmap==null) {
                task=new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);//将task任务存储起来,方便后续管理
            }
        }
    }
    //使用AsyncTask机制来加载网络图片
    public void showImageByAsyncTask(ImageView imageView,String url)
    {
        //从缓存中取出url值对应的图片
        Bitmap bitmap=loadBitmapFromLruCache(url);
        //缓存中存在图片时直接将其显示出来即可
        //如果图片没有缓存,这时才加载网络图片,并将其缓存下来
        if(bitmap==null) {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            imageView.setImageBitmap(bitmap);//列表中item显示变化时更新图片显示
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
            /*****磁盘加载或者网络加载图片,最终会将结果添加到应用内存中*****/
            Bitmap bitmap=loadBitmap(url,100,100);
            return bitmap;
        }
        //图片加载完后进行显示处理,显示到对应的item中
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //如果当前任务被取消了,直接退出任务
            if(isCancelled()) return;
            super.onPostExecute(bitmap);
            //通过tag获取imagview对象
            ImageView imageView= (ImageView) mRecyclerView.findViewWithTag(mUrl);
            if(imageView!=null&&bitmap!=null) {
                imageView.setImageBitmap(bitmap);//缓存结束的那一次会显示图片
            }
            mTask.remove(this);//任务执行完后将进程从集合中移除
        }
    }
}
