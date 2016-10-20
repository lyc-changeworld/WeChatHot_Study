package com.example.achuan.materialdesign_study.base;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by achuan on 16-9-21.
 * 功能：基础操作类,实现presenter和view层的弱引用控制
 */

public abstract class BasePresenter<T extends BaseView> {
    protected Reference<T> mViewRef;//View接口类的弱引用
    public void attachView(T view)
    {
        mViewRef=new WeakReference<T>(view);//建立关联
    }
    public void detachView()
    {
        if(mViewRef!=null)
        {
            mViewRef.clear();
            mViewRef=null;
        }
    }
    protected T getView()
    {
        return mViewRef.get();
    }
    public boolean isViewAttached()
    {
        return  mViewRef!=null&&mViewRef.get()!=null;
    }
}