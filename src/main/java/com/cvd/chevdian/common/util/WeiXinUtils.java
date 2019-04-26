package com.cvd.chevdian.common.util;


import com.alibaba.fastjson.JSONObject;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.constant.WeixinConstants;
import com.cvd.chevdian.config.WeixinProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
@Slf4j
public class WeiXinUtils {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    WeixinProperties weixinProperties;

//    @Autowired
//    private PushTemplate pushTemplate;
//
//    @Autowired
//    private CustomMsgTemplate customMsgTemplate;


    /**
     * 获取token
     *
     * @return
     * @throws Exception
     */
    public String getAccessToken() {
        String access_token = null;
        try {
            access_token = redisUtil.getString(GlobalConstant.WEIXIN_ACCESSTOKEN);
        } catch (Exception e) {
            log.error("", e);
        }
        if (!StringUtils.isEmpty(access_token)) {
            log.info("access_token from redis = {}", access_token);
            return access_token;
        }

        String url = WeixinConstants.ACCESS_TOKEN_URL
                .replace("APPID", weixinProperties.getAppid())
                .replace("APPSECRET", weixinProperties.getAppsecret());
        String result = HttpUtil.doGet(url);
        log.info("get accessToken string {}", result);
        access_token = JSONObject.parseObject(result).getString("access_token");
        log.info("access_token from wx = {}" + access_token);
        //保存在redis里 设置时间为115分钟
        try {
            redisUtil.putToString(GlobalConstant.WEIXIN_ACCESSTOKEN, access_token, 60 * 115);
        } catch (Exception e) {
            log.error("", e);
        }

        return access_token;
    }

    /**
     * 通过微信用户id获取用户名
     *
     * @param openid 微信用户id
     * @return 微信信息
     * @throws Exception
     */
    public Map<String, String> getNickname(String openid) {
        log.info("获取用户信息={}", openid);
        String resultStr = HttpUtil.doGet(WeixinConstants.GET_USER_INFO
                .replace("ACCESS_TOKEN", getAccessToken())
                .replace("OPENID", openid));

        JSONObject json = JSONObject.parseObject(resultStr);
        if (json.containsKey("errcode")) {
            log.error("获取用户信息失败={}", json);
            if (40001 == Integer.valueOf(json.getString("errcode"))) {
                log.error("access_token 失效");
                redisUtil.delect(GlobalConstant.WEIXIN_ACCESSTOKEN);
//                getNickname(openid);
            }
            return null;
        }

        String nickname = json.getString("nickname");
        if (!StringUtils.isEmpty(nickname)) {
            String base64 = Base64.getBase64(nickname);
            json.put("nickname_base64", base64);
        }
        return (Map) json;
    }

    /**
     * 发送客服消息
     *
     * @author xiaowuge
     * @date 2019年1月3日
     * @version 1.0
     */
//    public void sendCustomMsg(int type, Map<String, Object> result) throws Exception {
//        logger.info("微信模板消息推送=============================================>");
//        logger.info("模板id={}，内容={}", type, result);
//        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + getAccessToken();
//        //创建HttpClient执行对象
//        //创建httpPost请求
//        JSONObject jsonString = null;
//        String json = null;
//        // 1 发送文本消息
//        // 2 发送图文消息（点击跳转到外链）
//        // 3 发送图文消息（点击跳转到图文消息页面）
//        switch (type) {
//            case 1:
//                try {
//                    json = customMsgTemplate.textMsg(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 2:
//                try {
//                    jsonString = customMsgTemplate.newsMsg(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 3:
//                try {
//                    jsonString = customMsgTemplate.mpnewsMsg(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            default:
//                break;
//        }
//        HttpResponse response = HttpUtils.sendPost(url, json);
//        String rs = EntityUtils.toString(response.getEntity(), "utf-8");
//        logger.info("返回结果={}", rs);
//    }

//    public void sendTemplate(int type, Map<String, String> result) throws Exception {
//        logger.info("微信模板消息推送=============================================>");
//        logger.info("模板id={}，内容={}", type, result);
//        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + getAccessToken();
//        //发送get请求步骤:
//        //1 创建httpClient执行对象
//        HttpClient execution = new DefaultHttpClient();
//        //2 创建httpGet请求
//        HttpPost httpPost = new HttpPost(url);
//        JSONObject jsonString = null;
//        // 1 互助和理赔审核通过通知 【审核通过提醒】
//        // 2 定损通过支付通知 【定损通过提醒】
//        // 3 维修完成取车通知 【维修已完成提醒】
//        // 4 余额不足通知 【余额不足提醒】
//        // 5 分摊推送通知
//        // 6 活动推送通知
//        // 7 加入车V互助支付推送通知【未支付通知提醒】
//        // 8 加入车V互助添加完好车辆照片推送通知
//        // 9 审核失败通知 【审核未通过提醒】
//        // 10 订单状态更新通知 【订单状态更新】
//        // 11 车辆进入观察期通知 【车辆进入观察期提醒】
//        // 12 账单生成通知
//        // 13 车辆保障中通知
//        // 14【退出互助计划通知】
//        // 15【报告生成通知】
//        // 16【互助额度不足500】
//        // 17【充值成功提醒】
//        // 18【月账单提醒】
//        // 19【邀请成功提醒】
//        // 20【退款成功推送消息】
//        // 21【29元包年】
//        switch (type) {
//            case 1:
//                try {
//                    jsonString = pushTemplate.auditNotification(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 2:
//                try {
//                    jsonString = pushTemplate.paymentNotice(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 3:
//                try {
//                    jsonString = pushTemplate.maintenanceNotification(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 4:
//                try {
//                    jsonString = pushTemplate.lackBalance(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 5:
//                try {
//                    jsonString = pushTemplate.shareMoney(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 6:
//                try {
//                    jsonString = pushTemplate.activitiesPush(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 7:
//                try {
//                    jsonString = pushTemplate.paymentHelp(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 8:
//                try {
//                    jsonString = pushTemplate.addCarImg(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 9:
//                try {
//                    jsonString = pushTemplate.examineFail(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 10:
//                try {
//                    jsonString = pushTemplate.orderUpdate(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 11:
//                try {
//                    jsonString = pushTemplate.carObservationInform(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 12:
//                try {
//                    jsonString = pushTemplate.billNotifications(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 13:
//                try {
//                    jsonString = pushTemplate.carGuaranteeNotice(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 14:
//                try {
//                    jsonString = pushTemplate.quitRescue(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 15:
//                try {
//                    jsonString = pushTemplate.createNotice(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 16:
//                try {
//                    jsonString = pushTemplate.lackAmount(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 17:
//                try {
//                    jsonString = pushTemplate.rechargeSuccess(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 18:
//                try {
//                    jsonString = pushTemplate.monthBill(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 19:
//                try {
//                    jsonString = pushTemplate.invitationSuccess(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 20:
//                try {
//                    jsonString = pushTemplate.refundNotice(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 21:
//                try {
//                    jsonString = pushTemplate.yearActivity(result);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            default:
//        }
//        String menuStr = jsonString.toString();
//        httpPost.setEntity(new StringEntity(menuStr, "utf-8"));
//        //3 通过执行对象传入执行请求,发送请求,获取响应对象
//        HttpResponse response = execution.execute(httpPost);
//        //400 200 404 500
//        int statusCode = response.getStatusLine().getStatusCode();
//        logger.info("statusCode={}", statusCode);
//        //4 通过响应对象获取响应实体,把响应实体转换json字符串
//        HttpEntity responseEntity = response.getEntity();
//        String jsonStr = EntityUtils.toString(responseEntity);
//        logger.debug("返回结果=", jsonStr);
//    }


    /**
     * 生成用户专属永久字符串二维码
     * {"action_name": "QR_LIMIT_STR_SCENE", "action_info": {"scene": {"scene_str": "test"}}}
     */
    public String createForeverQrcode(String eventKey,String accessToken) throws Exception {
        JSONObject json = new JSONObject();
        json.put("action_name", "QR_LIMIT_STR_SCENE");
        JSONObject str = new JSONObject();
        str.put("scene_str", eventKey);
        JSONObject info = new JSONObject();
        info.put("scene", str);
        json.put("action_info", info);
        String rs = HttpUtil.doPostJson(WeixinConstants.QRCODE_URL
                        .replace("ACCESS_TOKEN", accessToken),
                json.toJSONString());
        String ticket = JSONObject.parseObject(rs).getString("ticket");
        return updateTicketToOSS(ticket);
    }

    /**
     * 生成用户临时字符串二维码
     * {"expire_seconds": 604800, "action_name": "QR_STR_SCENE", "action_info": {"scene": {"scene_str": "test"}}}
     */
    public String createTempQrcode(String eventKey,String accessToken) throws Exception {
        JSONObject json = new JSONObject();
        json.put("action_name", "QR_STR_SCENE");
        JSONObject str = new JSONObject();
        str.put("scene_str", eventKey);
        JSONObject info = new JSONObject();
        info.put("scene", str);
        json.put("action_info", info);
        json.put("expire_seconds", 24 * 60 * 60);
        String rs = HttpUtil.doPostJson(WeixinConstants.QRCODE_URL
                        .replace("ACCESS_TOKEN", accessToken),
                json.toJSONString());
        String ticket = JSONObject.parseObject(rs).getString("ticket");
        return updateTicketToOSS(ticket);
    }

    /**
     * 保存到oss
     */
    private String updateTicketToOSS(String ticket) throws Exception {
        HttpClient hClient = new DefaultHttpClient();
        String ticketUrl = WeixinConstants.QRCODE_URL_GET.replace("TICKET", URLEncoder.encode(ticket, "utf-8"));
        HttpGet httpGet = new HttpGet(ticketUrl);
        HttpResponse response = hClient.execute(httpGet);
        if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
            HttpEntity entity = response.getEntity();
            InputStream in = entity.getContent();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String newFileName = uuid + ".jpg";
            // 上传至阿里云
            boolean uploadResult = FileUtil.saveImg(GlobalConstant.OSS_AGENT_IMG_URL, newFileName, in);
            log.info("上传到阿里云的图片({})结果为: {}", GlobalConstant.OSS_AGENT_IMG_URL + newFileName, uploadResult);
            if (!uploadResult) {
                updateTicketToOSS(ticket);
            }
            return newFileName;
        }
        return null;
    }

    /**
     * js 微信参数
     *
     * @return
     */
    public SortedMap<String, String> toWxJsInfo(String url_1, String url) {
        String currTime = String.valueOf(new Date().getTime());
        String jsapi_ticket = getTicket();//获取Ticket
        String noncestr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);//时间戳和随机字符串
        String str = "jsapi_ticket=" + jsapi_ticket + "&noncestr=" + noncestr + "&timestamp=" + currTime + "&url=" + url;//将参数排序并拼接字符串
        //生成签名
        SortedMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("appId", weixinProperties.getAppid());
        treeMap.put("timestamp", currTime);
        treeMap.put("nonceStr", noncestr);
        treeMap.put("jsApiList", "[onMenuShareTimeline,onMenuShareAppMessage,onMenuShareQQ,onMenuShareWeibo,onMenuShareQZone]");
        treeMap.put("signature", SHA1(str));//将字符串进行sha1加密
        return treeMap;
    }

    private String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (byte aMessageDigest : messageDigest) {
                String shaHex = Integer.toHexString(aMessageDigest & 0xFF);
                if (shaHex.length() < 2)
                    hexString.append(0);
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("sha1 error", e.getMessage(), e);
        }
        return "";
    }

    private String getTicket() {
        String ticket = redisUtil.getString("jsapi_ticket");
        if (StringUtils.isNotEmpty(ticket)) {
            log.info("ticket from redis = {}", ticket);
            return ticket.substring(1, ticket.length() - 1);
        }
        String wxrs = HttpUtil.doGet(WeixinConstants.JS_GET_TICKET.replace("ACCESS_TOKEN", getAccessToken()));
        JSONObject demoJson = JSONObject.parseObject(wxrs);
        ticket = demoJson.getString("ticket");
        // 缓存ticket
        log.info("ticket from wx = {}", ticket);
        redisUtil.putToString("jsapi_ticket", ticket, 7170);
        return ticket;
    }
}
