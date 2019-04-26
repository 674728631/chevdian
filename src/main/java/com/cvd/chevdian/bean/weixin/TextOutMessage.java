package com.cvd.chevdian.bean.weixin;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="xml")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TextOutMessage  extends  OutMessage{
    // 文本消息内容
    private String Content;
}
