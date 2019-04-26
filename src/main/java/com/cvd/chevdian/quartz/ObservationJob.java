package com.cvd.chevdian.quartz;

import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.util.DateUtil;
import com.cvd.chevdian.common.util.MapUtil;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.controller.weixin.WeixinPayController;
import com.cvd.chevdian.mapper.PayResultMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 处理定时调度任务的统一入口
 */
@Slf4j
@Component
public class ObservationJob implements Job {

    @Autowired
    @Qualifier("schedulerFactory")
    private SchedulerFactoryBean schedulerFactory;

    @Autowired
    WeiXinUtils weiXinUtils;
    @Autowired
    PayResultMapper payResultMapper;
    @Autowired
    WeixinPayController weixinPayController;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        log.info("转入观察期【定时任务】开始......{}", jobDataMap);
        Map<String, Object> params = (Map<String, Object>) jobDataMap.get("params");//TODO 对象注入失败
        Scheduler sche = schedulerFactory.getScheduler();
        String jobName = params.get("jobName").toString();
        SimpleDateFormat format = new SimpleDateFormat(GlobalConstant.DEFAULT_FORMAT);
        try {
            Map map = new HashMap();
            //修改车辆保障时间
            Map<String, Object> carMap = payResultMapper.findCarById(new Integer(params.get("carId").toString()));

            //获取用户信息
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("id", carMap.get("customerId"));
            Map<String, Object> customer = payResultMapper.findUser(reqMap);
            //进入保障中
            reqMap.clear();
            reqMap.put("customerId", customer.get("id"));
            reqMap.put("customerPN", customer.get("customerPN"));
            reqMap.put("source", customer.get("source"));
            reqMap.put("createAt", customer.get("timeJoin"));
            reqMap.put("currentStatus", customer.get("status"));
            reqMap.put("optTime", format.format(new Date()));
            reqMap.put("optType", 6);
            reqMap.put("optDesc", "进入保障中");
            reqMap.put("recordTime", format.format(new Date()));
            payResultMapper.saveUserCustomerLog(reqMap);

            String typeGuarantee;
            if (null == params.get("typeGuarantee") || "".equals(params.get("typeGuarantee")))
                typeGuarantee = "1";
            else
                typeGuarantee = params.get("typeGuarantee").toString();

            typeGuarantee = null != carMap.get("typeGuarantee") ? carMap.get("typeGuarantee").toString() : typeGuarantee;
            if (!StringUtils.isEmpty(carMap.get("messageFlag"))) {
                Integer messageFlag = (Integer) carMap.get("messageFlag");
                if (!StringUtils.isEmpty(params.get("openid"))) {
                    if (messageFlag == 5 || messageFlag == 7) {
                        map.put("messageFlag", 2);
                    }
                } else {
                    if (messageFlag == 5) {
                        map.put("messageFlag", 7);
                    }
                }
            }

            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat yearTime = new SimpleDateFormat("yyyy-MM-dd");
            if (carMap.get("timeBegin") != null
                    && DateUtil.getYearDate((Date) carMap.get("timeBegin")).compareTo(new Date()) == 1) {
                // 有timeBegin，并且timeBegin未满一年
                map.put("timeEnd", "2".equals(typeGuarantee) ? yearTime.format(DateUtil.getYearDate((Date) carMap.get("timeBegin"))) : "");
            } else {
                map.put("timeBegin", yearTime.format(new Date()));
                map.put("timeEnd", "2".equals(typeGuarantee) ? yearTime.format(DateUtil.getYearDate(new Date())) : "");
            }
            if (null != params.get("yearFlag") && "1".equals(params.get("yearFlag").toString()))
                map.put("timeEnd", yearTime.format(DateUtil.getYearDate(new Date())));

            map.put("id", params.get("carId").toString());
            map.put("status", 20);
            payResultMapper.updateCar(map);
            weixinPayController.updateDayNumber("guaranteeNum", 1);
            //删除定时任务
            weixinPayController.removeJob(sche, jobName);
            //微信推送
            map.clear();
            log.info("观察期结束=====:  {}", params);
            if (!StringUtils.isEmpty(customer.get("openId"))) {

                // 发送车辆进入保障的通知
                Map dictionary = payResultMapper.findDictionarySingle(MapUtil.build().put("type", "observationTime").over());
                Map<String, String> param = new HashMap<>();
//				param.put("openid", (String)params.get("openid"));
                param.put("openid", (String) customer.get("openId"));
                param.put("licensePlateNumber", (String) carMap.get("licensePlateNumber"));
                param.put("observationTime", dictionary.get("value").toString());
//                weiXinUtils.sendTemplate(13, param);
            }

            log.info("转入观察期【定时任务】结束......");
        } catch (Exception e) {
            log.error("", e);
        }
    }

}