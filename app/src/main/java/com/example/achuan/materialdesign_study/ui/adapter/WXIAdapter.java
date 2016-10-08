package com.example.achuan.materialdesign_study.ui.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.achuan.materialdesign_study.R;
import com.example.achuan.materialdesign_study.model.bean.WXItemBean;
import com.example.achuan.materialdesign_study.util.ImageLoader;
import com.example.achuan.materialdesign_study.widget.SquareImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by achuan on 16-10-5.
 * 功能：实现列表中数据的适配
 * 技巧：提高列表的显示效果,滑动时,停止加载数据;停止时,只加载当前显示范围类的数据
 * 注意事项：第一次显示列表时并没有发生滑动事件,此时并不会触发滑动状态改变的监听事件,所以需要
 *　　　　　在scroll方法中添加一个初始化第一屏的图片加载事件
 */
public class WXIAdapter extends RecyclerView.Adapter<WXIAdapter.ViewHolder> {

    //定义一个全局变量,保证只创建一个LruCache对象
    private ImageLoader mImageLoader;
    private int mStart,mEnd;//定义item加载的起始和结束的位置序号
    public static List<String> URLS=new ArrayList<>();//存储所有的图片链接地址
    private boolean mFirstIn=true;//为标志变量设置true,标志第一次显示


    private LayoutInflater mInflater;//创建布局装载对象来获取相关控件（类似于findViewById()）
    protected List<WXItemBean> mList;//存储数据集合
    private Context mContext;//显示框面

    /***********
     * 自定义item的点击事件接口
     *********/
    public interface OnItemClickListener {
        void onItemClick(View view, int postion);
        void onItemLongClick(View view, int postion);
    }
    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /*构造方法*/
    public WXIAdapter(Context context, List<WXItemBean> mList, RecyclerView recyclerView) {
        this.mContext = context;
        this.mList = mList;
        //通过获取context来初始化mInflater对象
        mInflater = LayoutInflater.from(context);
        mImageLoader=new ImageLoader(recyclerView);

        if(mList.size()>0)
        {
            //存储所有的图片访问链接地址
            for (int i = 0; i <mList.size() ; i++) {
                URLS.add(mList.get(i).getPicUrl());
            }
        }

        //为列表的滑动事件注册监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState==RecyclerView.SCROLL_STATE_IDLE)//滑动停止的状态,加载数据
                {
                    //将指定范围中的图片加载显示出来
                    mImageLoader.loadImages(mStart,mEnd);
                }
                else {//其他状态停止加载数据
                    mImageLoader.cancelAllTasks();
                }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // dy :正  列表向上划动
                // dy :负  列表向下划动 上下滑动时dx为正
                //当前列表界面最上面的item的序号（小号） 序号从0开始
                mStart=((LinearLayoutManager) recyclerView
                        .getLayoutManager()).findFirstVisibleItemPosition();
                //当前列表界面最下面的item的序号（大号）　
                mEnd = ((LinearLayoutManager) recyclerView
                        .getLayoutManager()).findLastVisibleItemPosition();
                //int mCurrent=recyclerView.getLayoutManager().
                /***第一次显示列表时会先加载第一屏的图片***/
                if(mFirstIn){
                    mFirstIn=false;//已经不是第一次加载列表
                    mImageLoader.loadImages(mStart,mEnd);
                }
            }
        });
    }
    //适配器中数据集中的个数
    public int getItemCount() {
        return mList.size();
    }



    /****
     *
     * item第一次显示时,才创建其对应的viewholder进行控件存储,之后直接使用即可
     *
     * ****/
    //先创建ViewHolder
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = mInflater.inflate(R.layout.item_wechat, parent, false);//载入item布局
        ViewHolder viewHolder = new ViewHolder(view);//创建一个item的viewHoler实例
        return viewHolder;
    }
    /****当前界面中出现了item的显示更新时执行该方法（即有item加入或者移除界面）
     *
     * 该方法的执行顺序　　早于　　onScrolled（）方法
     *
     * ****/
    //绑定ViewHolder
    public void onBindViewHolder(final ViewHolder holder, final int postion) {
        //再通过viewHolder中缓冲的控件添加相关数据
        WXItemBean bean=mList.get(postion);//从数据源集合中获得对象
        //获取对应item的图片链接
        String url=bean.getPicUrl();
        /*通过设置tag来保证图片和url的对应显示,防止网络加载时的时序错乱*/
        holder.mIvWechatItemImage.setTag(url);
        //使用AsyncTask来加载图片
        mImageLoader.showImageByAsyncTask(holder.mIvWechatItemImage,url);
        //绑定数据
        holder.mTvWechatItemTitle.setText(mList.get(postion).getTitle());
        holder.mTvWechatItemFrom.setText(mList.get(postion).getDescription());
        holder.mTvWechatItemTime.setText(mList.get(postion).getCtime());
        /*******为itemView设置接口监听******/
        setItemEvent(holder);
    }
    /*将监听回调事件写成方法*/
    protected void setItemEvent(final ViewHolder holder) {
        if (mOnItemClickListener != null) {
            //监听点击事件
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int layoutPostion = holder.getLayoutPosition();//更新item的位置
                    mOnItemClickListener.onItemClick(v, layoutPostion);
                }
            });
            //监听长按事件
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int layoutPostion = holder.getLayoutPosition();//更新item的位置
                    mOnItemClickListener.onItemLongClick(v, layoutPostion);
                    return false;
                }
            });
        }
    }
    /*创建自定义的ViewHolder类*/
    public  class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.iv_wechat_item_image)
        SquareImageView mIvWechatItemImage;
        @BindView(R.id.tv_wechat_item_title)
        TextView mTvWechatItemTitle;
        @BindView(R.id.tv_wechat_item_from)
        TextView mTvWechatItemFrom;
        @BindView(R.id.tv_wechat_item_time)
        TextView mTvWechatItemTime;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
