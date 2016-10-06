package com.example.achuan.materialdesign_study.model;

/**
 * Created by achuan on 16-10-5.
 * 功能：数据实体类,用来存储解析后的JSON数据
 * 例子：
 * {
 "code": 200,   //返回状态码
 "msg": "success",  //返回状态消息
 "newslist": [
 {
 "ctime": "2016-03-31",  //文章发布时间
 "title": "奇虎360宣布通过私有化决议",  //文字标题
 "description": "互联网头条",  //来源及描述
 "picUrl": "http://t1.qpic.cn/mblogpic/f01a972dbcc1060fd456/2000", //文章插图，若空为默认
 "url": "http://mp.weixin.qq.com/s?__biz=MjM5OTMyODA2MA==&idx=1&mid=402594468&sn=5cd644536b472a283cc1d3f5124a0cab"
 },  //文章URL
 {
 "ctime": "2016-03-31",
 "title": "小本生意做什么挣钱十七大小本生意推荐",
 "description": "创业最前线",
 "picUrl": "http://zxpic.gtimg.com/infonew/0/wechat_pics_-4225297.jpg/640",
 "url": "http://mp.weixin.qq.com/s?__biz=MzA3NjgzNDUwMQ==&idx=2&mid=401864059&sn=cfa082e38ba38c7e673b1ce0a075faee"
 },
 */
public class WXItemBean {
    private String ctime;//文章发布时间
    private String title;//文字标题
    private String description;//来源及描述
    private String picUrl;//文章插图链接,若空为默认
    private String url;//文章URL

    public WXItemBean(String ctime, String title, String description, String picUrl, String url) {
        this.ctime = ctime;
        this.title = title;
        this.description = description;
        this.picUrl = picUrl;
        this.url = url;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
