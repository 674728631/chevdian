package com.cvd.chevdian.bean.weixin;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name="xml")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class OutMessage {
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
    */
    private String MsgType;
    // 消息id，64位整型
    private Long MsgId;
    // 图片链接
    private String PicUrl;
    // 图片消息媒体id，可以调用多媒体文件下载接口拉取数据
    @XmlElementWrapper(name="Image")
    private String[] MediaId;
    // 文本消息内容
    private String Content;

    @Override
    public String toString() {
        return "OutMessage{" +
                "ToUserName='" + ToUserName + '\'' +
                ", FromUserName='" + FromUserName + '\'' +
                ", CreateTime=" + CreateTime +
                ", MsgType='" + MsgType + '\'' +
                ", MsgId=" + MsgId +
                '}';
    }

    public OutMessage(InMessage inMessage) {
        ToUserName = inMessage.getFromUserName();
        FromUserName = inMessage.getToUserName();
        CreateTime = new Date().getTime();
    }
}
