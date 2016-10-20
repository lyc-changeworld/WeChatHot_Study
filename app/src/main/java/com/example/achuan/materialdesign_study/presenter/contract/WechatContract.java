package com.example.achuan.materialdesign_study.presenter.contract;

import com.example.achuan.materialdesign_study.base.BaseView;

/**
 * Created by achuan on 16-9-29.
 * 功能：展示各个层要实现的方法,声明回调接口
 * 一般只装view和presenter层的方法
 */
public class WechatContract {
    //view层要实现的抽象方法
    interface View extends BaseView {


    }
    //Presenter层要实现的抽象方法
    interface Presenter{
        void loadData();
    }

}
