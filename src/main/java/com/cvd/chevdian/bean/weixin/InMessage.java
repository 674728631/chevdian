package com.cvd.chevdian.bean.weixin;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class InMessage {
    // 开发者微信号
    private String ToUserName;
    // 	发送方帐号（一个OpenID）
    private String FromUserName;
    // 消息创建时间
    private Long CreateTime;
    /*
     * 消息类型
     * text
     * image
     * voice
     * video
     * music
     * event
     */
    private String MsgType;
    // 文本消息内容
    private String Content;
    // 消息id，64位整型
    private Long MsgId;

    // 图片链接（由系统生成）
    private String PicUrl;
    // 图片消息媒体id，可以调用多媒体文件下载接口拉取数据
    private String MediaId;
    //
    private String Format;
    // 事件类型，subscribe(订阅)、unsubscribe(取消订阅)
    private String Event;
    // 事件KEY值，与自定义菜单接口中KEY值对应
    private String EventKey;

    @Override
    public String toString() {
        return "InMessage{" +
                "ToUserName='" + ToUserName + '\'' +
                ", FromUserName='" + FromUserName + '\'' +
                ", CreateTime=" + CreateTime +
                ", MsgType='" + MsgType + '\'' +
                ", Content='" + Content + '\'' +
                ", MsgId=" + MsgId +
                ", PicUrl='" + PicUrl + '\'' +
                ", MediaId='" + MediaId + '\'' +
                '}';
    }
}
