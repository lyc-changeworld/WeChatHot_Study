package com.example.achuan.materialdesign_study.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.achuan.materialdesign_study.app.Constants;
import com.example.achuan.materialdesign_study.R;
import com.example.achuan.materialdesign_study.ui.adapter.WXIAdapter;
import com.example.achuan.materialdesign_study.model.bean.WXItemBean;
import com.example.achuan.materialdesign_study.model.http.WechatAsyncTask;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    String httpArg = "num="+ Constants.FIRST_NUM_OF_PAGE;
    private boolean mFirstIn=true;//是否第一次显示的标志

    @BindView(R.id.id_recyclerView)
    RecyclerView mIdRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(mFirstIn)
        {
            WechatAsyncTask mWechatAsyncTask;
            mWechatAsyncTask=new WechatAsyncTask();
            mWechatAsyncTask.execute(Constants.URL,httpArg);
            mWechatAsyncTask.setHttpCallbackListener(new WechatAsyncTask.HttpCallbackListener() {
                @Override
                public void onFinish(List<WXItemBean> wxItemBeanList) {
                    //如果是第一打开活动,先加载20页起
                    mFirstIn=false;//第一次加载成功时,标志才改变
                    //为列表绑定数据
                    recyclerViewSetAdapter(wxItemBeanList);
                }
                @Override
                public void onError() {
                    Toast.makeText(MainActivity.this, "加载失败...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //为列表添加适配器,并执行其它的初始化操作
    public void recyclerViewSetAdapter(List<WXItemBean> wxItemBeanList)
    {
        //创建一个适配器
        WXIAdapter adapter=new WXIAdapter(MainActivity.this,wxItemBeanList,mIdRecyclerView);
        mIdRecyclerView.setAdapter(adapter);//为列表添加适配器
        //设置相关布局管理
        LinearLayoutManager linearManager=new LinearLayoutManager
                (MainActivity.this,LinearLayoutManager.VERTICAL,false);//设置布局方式为线性居中布局
        mIdRecyclerView.setLayoutManager(linearManager);//应用到RecyclerView中去
    }


}
