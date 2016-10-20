package com.example.achuan.materialdesign_study.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by achuan on 16-9-21.
 * 功能：基础活动类,初始化活动的一些基本抽象方法
 */
public abstract class MVPBaseActivity<V extends BaseView,T extends BasePresenter<V>>extends AppCompatActivity
{
    protected T mPresenter;//Presenter对象的引用变量
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //允许为空，不是所有都要实现MVP模式
        if(createPresenter()!=null) {
            mPresenter = createPresenter();//创建Presenter对象
            mPresenter.attachView((V) this);//View与Presenter建立关联
        }
        setContentView(getLayout());//载入布局文件
        ButterKnife.bind(this);//初始化控件
        initEventAndData();//初始化事件和数据
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPresenter!=null)
        {
            mPresenter.detachView();
        }
    }
    //创建具体Presenter的抽象方法,具体实现方法由子类实现
    protected abstract T createPresenter();
    //获取布局文件的抽象方法,具体实现方法由子类实现
    protected abstract int getLayout();
    //创建初始化事件和数据的抽象方法,具体实现方法由子类实现
    protected abstract void initEventAndData();
}