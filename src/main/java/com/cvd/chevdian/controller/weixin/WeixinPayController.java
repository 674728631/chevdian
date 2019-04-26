package com.cvd.chevdian.controller.weixin;


import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.util.*;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.mapper.CarMapper;
import com.cvd.chevdian.mapper.PayResultMapper;
import com.cvd.chevdian.mapper.RecordRechargeMapper;
import com.cvd.chevdian.quartz.ObservationJob;
import com.cvd.chevdian.service.CarService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Controller
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class WeixinPayController {

    @Autowired
    CarService carService;
    @Autowired
    CarMapper carMapper;
    @Autowired
    RecordRechargeMapper recordRechargeMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    WxUtil wxUtil;
    @Autowired
    TaskExecutor taskExecutor;
    @Autowired
    PayResultMapper payResultMapper;

    @Autowired
    @Qualifier("schedulerFactory")
    private SchedulerFactoryBean schedulerFactory;

    SimpleDateFormat format = new SimpleDateFormat(GlobalConstant.LONG_FORMAT);
    SimpleDateFormat default_format = new SimpleDateFormat(GlobalConstant.DEFAULT_FORMAT);

    private static String JOB_GROUP_NAME = "JOB_GROUP_SYSTEM";
    private static String TRIGGER_GROUP_NAME = "TRIGGER_GROUP_SYSTEM";

    @Value("${chevhuzhu.rechargeNum}")
    private Integer rechargeNum;
    @Value("${chevhuzhu.url}")
    private String chevhuzhu;

    /**
     * 微信支付-- 统一下单
     */
    @PostMapping(value = "/weChatPay")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Wrapper<String> wxPayCar(HttpServletRequest request, @RequestBody Map<String, Object> params) throws Exception {
        UserDistributor loginUser = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        log.info("{}支付进来了===>", loginUser);
        String json = "";
//        String[] carIds = request.getParameterValues("CarId");  //选择的车辆id
//        String amountId = request.getParameter("amountId");  //金额的Id
        String[] carIds = (String[]) params.get("CarId");  //选择的车辆id
        // 判断车辆个数
        if (carIds.length == 0 || carIds.length > rechargeNum)
            throw new IllegalArgumentException("选择车辆数量错误");

        String outTradeNo = RandomUtil.getUUID();
        log.info("预订单号outTradeNo----------------{}", outTradeNo);

        String amountId = (String) params.get("amountId");  //金额的Id
        BigDecimal a = new BigDecimal("0");
        StringBuilder orderNo = new StringBuilder();
        for (String carId : carIds) {
            if ("1".equals(amountId)) {
                a = new BigDecimal("9");
            } else if ("4".equals(amountId)) {
                a = new BigDecimal("29");
            } else if ("3".equals(amountId)) {
                Map<String, Object> rMap = carService.getPayAmount(carId);
                a = new BigDecimal("99").subtract(new BigDecimal(String.valueOf(rMap.get("amount"))));
            } else
                throw new IllegalArgumentException("充值金额错误");

            Map<String, Object> cmap = new HashMap<>();
            cmap.put("id", carId);
            Car car = carMapper.getCarById(Integer.valueOf(carId));
            if (null == car)
                throw new IllegalArgumentException("充值车辆选择错误");
            Integer typeGuarantee = car.getTypeGuarantee();
            String licensePlateNumber = car.getLicensePlateNumber();

            if (2 == typeGuarantee)
                return WrapMapper.wrap(ErrorCodeEnum.GL99990118.code(), ErrorCodeEnum.GL99990118.msg(), licensePlateNumber + "正在包年中，无法充值");

            Map<String, Object> pMap = new HashMap<>();
            pMap.put("customerId", loginUser.getId());
            pMap.put("carId", carId);
            pMap.put("amt", a);
            pMap.put("type", DBDict.RECHARGE_TYPE_1);
            pMap.put("status", DBDict.RECHARGE_STATUS_2);
            pMap.put("eventNo", outTradeNo);
            pMap.put("eventType", null == car.getTimeBegin() ? "1" : "2");
            recordRechargeMapper.saveSingle(pMap);
            Long rId = Long.valueOf(pMap.get("id").toString());
            if (orderNo.length() == 0)
                orderNo.append(rId);
            else
                orderNo.append("|" + rId);
        }
        redisUtil.putToString(outTradeNo, orderNo.toString());

        // int payMoney_int = 1;
        //支付时间,回调(通知地址),ip,商品内容,openid,商户订单号(订单id_随机值),支付金额,随机字符,设备号
        String result = wxUtil.sendPrepay(Long.valueOf(format.format(new Date())),
                chevhuzhu + "/wxPayCarResult",
                "127.0.0.1", "渠道充值",
                loginUser.getOpenId(),
                outTradeNo,
                a.multiply(DBDict.BASE_PAY).intValue(),
                WxUtil.SuJiShu(),
                "车险");

        Map<String, Object> wxMap = WxUtil.resolveXml(result);//解析xml
        log.info("统一下单返回xml:{}", wxMap);

        // 获取prepay_id 封装返回吊起H5支付所需参数
        if ("SUCCESS".equals(wxMap.get("prepay_id")) && wxMap.get("prepay_id") != null)
            json = String.valueOf(wxUtil.coverH5Pay(wxMap.get("prepay_id")));
        else
            return WrapMapper.error("微信支付失败");
        return WrapMapper.ok(json);
    }


    @PostMapping(value = "/weChatPay1")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public Wrapper<String> wxPayCar1(HttpServletRequest request, @RequestBody Map<String, Object> params) throws Exception {
        UserDistributor loginUser = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        log.info("{}支付进来了===>", loginUser);
        String json = "";
//        String[] carIds = request.getParameterValues("CarId");  //选择的车辆id
//        String amountId = request.getParameter("amountId");  //金额的Id
        List<String> carIds = (List<String>) params.get("CarId");  //选择的车辆id
        // 判断车辆个数
        if (carIds.size() == 0 || carIds.size() > rechargeNum)
            throw new IllegalArgumentException("选择车辆数量错误");

        String outTradeNo = RandomUtil.getUUID();
        log.info("预订单号outTradeNo----------------{}", outTradeNo);

        Integer amountId = (Integer) params.get("amountId");  //金额的Id
        BigDecimal a = new BigDecimal("0");
        StringBuilder orderNo = new StringBuilder();
        for (String carId : carIds) {
            if (1 == amountId) {
                a = new BigDecimal("9");
            } else if (4 == amountId) {
                a = new BigDecimal("29");
            } else if (3 == amountId) {
                Map<String, Object> rMap = carService.getPayAmount(carId);
                a = new BigDecimal("99").subtract(new BigDecimal(String.valueOf(rMap.get("amount"))));
            } else
                throw new IllegalArgumentException("充值金额错误");

            Map<String, Object> cmap = new HashMap<>();
            cmap.put("id", carId);
            Car car = carMapper.getCarById(Integer.valueOf(carId));
            if (null == car)
                throw new IllegalArgumentException("充值车辆选择错误");
            Integer typeGuarantee = car.getTypeGuarantee() == null ? 1 : car.getTypeGuarantee();
            String licensePlateNumber = car.getLicensePlateNumber();

            if (2 == typeGuarantee)
                return WrapMapper.wrap(ErrorCodeEnum.GL99990118.code(), ErrorCodeEnum.GL99990118.msg(), licensePlateNumber + "正在包年中，无法充值");

            Map<String, Object> pMap = new HashMap<>();
            pMap.put("customerId", loginUser.getId()); //TODO 存谁？
            pMap.put("carId", carId);
            pMap.put("amt", a);
            pMap.put("type", DBDict.RECHARGE_TYPE_1);
            pMap.put("status", DBDict.RECHARGE_STATUS_2);
            pMap.put("eventNo", outTradeNo);
            pMap.put("eventType", null == car.getTimeBegin() ? "1" : "2");
            recordRechargeMapper.saveSingle(pMap);
            Long rId = Long.valueOf(pMap.get("id").toString());
            if (orderNo.length() == 0)
                orderNo.append(rId);
            else
                orderNo.append("|" + rId);
        }
        redisUtil.putToString(outTradeNo, orderNo.toString());//TODO 是否存有效期

        int isZ = payCarResult(orderNo.toString());
        return WrapMapper.ok();
    }


    /**
     * 微信支付-- 支付结果通知
     */
    @PostMapping(value = "/wxPayCarResult")
    public void wxRePayOrder(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("支付回调进来了_____________________________________________");
            request.setCharacterEncoding("UTF-8");
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null)
                builder.append(line);
            Map<String, Object> wxReturnMap = WxUtil.resolveXml(builder.toString());
            if (null != wxReturnMap && "SUCCESS".equals(wxReturnMap.get("return_code"))) {
                String outTradeNo = wxReturnMap.get("out_trade_no").toString();
                String orderNo = redisUtil.getString(outTradeNo);
                if (orderNo == null) {
                    StringBuilder sb = new StringBuilder();
                    List<Map<String, Object>> rlist = recordRechargeMapper.slectByEventNo(outTradeNo);
                    rlist.forEach(l -> {
                        if (sb.length() == 0)
                            sb.append(l.get("id"));
                        else
                            sb.append("|" + l.get("id"));
                    });
                    orderNo = sb.toString();
                    if (orderNo.length() == 0)
                        throw new BusinessException(ErrorCodeEnum.GL99990119);
                }
                synchronized (orderNo) {
                    PrintWriter writer = response.getWriter();
                    int isZ = payCarResult(orderNo);
                    try {
                        if (isZ == 1) {
                            log.info("success--------------------");
                            redisUtil.delect(outTradeNo);
                            String noticeStr = WxUtil.setXML("SUCCESS", "ok");
                            writer.write(noticeStr);
                            writer.flush();
                        } else {
                            log.info("wxResult failure!");
                            String noticeStr = WxUtil.setXML("FAIL", "FAILURE");
                            writer.write(noticeStr);
                            writer.flush();
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            }
            System.out.println("wxResult-ok________________________________________________");
        } catch (Exception e) {
            log.error("支付通知处理失败=====》", e);
        }
    }

    private Integer payCarResult(String amountIdStr) throws SchedulerException {
        log.info("++++++++进入支付结果+++++++++");
        String[] amountIdArr = amountIdStr.split("\\|");
        for (String amountId : amountIdArr) {
            //根据充值id查询车辆信息和充值金额和状态
            Map<String, Object> carMap = payResultMapper.findCarByRecordRechargeId(amountId);

            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("id", carMap.get("customerId"));
            // 根据用户id查询用户信息及车辆信息
            Map<String, Object> customer = payResultMapper.findUser(reqMap);

            if (carMap != null && "2".equals(String.valueOf(carMap.get("rStatus")))) {//充值状态为未到账
                String carId = String.valueOf(carMap.get("id"));
                // 车辆id查询用户信息
                Map<String, Object> userMap = payResultMapper.selectUserByCarId(carMap);
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat yearTime = new SimpleDateFormat("yyyy-MM-dd");
                String date = time.format(new Date());
                BigDecimal oldAmount = String.valueOf(carMap.get("amtCooperation")).equals("null")
                        ? new BigDecimal(0) : new BigDecimal(String.valueOf(carMap.get("amtCooperation"))); //充值前余额
                log.info("充值前互助金={}", oldAmount);
                BigDecimal a = new BigDecimal(String.valueOf(carMap.get("amt")).equals("null") ? "0" : String.valueOf(carMap.get("amt"))); //充值金额
                log.info("充值金额={}", a);
                BigDecimal newAmount = oldAmount.add(a);

                Map<String, Object> map = new HashMap<>();
                map.put("timeRecharge", date);
                map.put("id", amountId);
                map.put("status", DBDict.RECHARGE_STATUS_1);
                map.put("description", "手机号为" + customer.get("customerPN") + "的会员为" + customer.get("licensePlateNumber") + "充值了" + a + "元");
                map.put("eventType", carMap.get("amtCooperation") == null ? 1 : 2);
                recordRechargeMapper.updateModel(map);


                /**/
                String status = String.valueOf(carMap.get("status"));
                final String oldStatus = status;
                log.info("充值时车辆状态oldStatus={}", oldStatus);
                status = status.equals("1") ? "13" : status.equals("30") ? "13" : status;
                log.info("充值后车辆状态status={}", status);
                String typeGuarantee = "1";
                BigDecimal sum = new BigDecimal("0");
                int f = 0; // 是否包年标识
                String amtCompensation = "1000";
                try {
                    Map<String, Object> rMap = carService.getPayAmount(carId);
                    Calendar calendar1 = Calendar.getInstance();
                    sum = new BigDecimal(String.valueOf(rMap.get("amount")));
                    log.info("用户历史充值总金额(包括本次充值和补贴)和第一次充值时间rMap={}", rMap);
                    //用户转包年
                    if (sum.compareTo(new BigDecimal("99")) >= 0) {
                        log.info("+++++++++++进入充值满99包年++++++++++");
                        calendar1.setTime(time.parse(String.valueOf(rMap.get("minT"))));//最近充值时间 TODO 时间有问题 getPayAmount查询
                        calendar1.add(Calendar.YEAR, 1);
                        Map<String, Object> m = new HashMap<>();
                        m.put("status", status);
                        if (carMap.get("timeEnd") == null) {
                            m.put("timeEnd", yearTime.format(calendar1.getTime()));
                        }
                        typeGuarantee = "2";
                        m.put("amtCooperation", newAmount);
                        m.put("typeGuarantee", typeGuarantee);
                        m.put("reasonSignout", "");
                        m.put("signoutMessageFlag", "0");
                        m.put("timeSignout", "1111-11-11 11:11:11");
                        m.put("payTime", date);
                        m.put("id", carId);
                        if (oldStatus.equals("1")) {
                            m.put("reJoinNum", "reJoinNum");
                            log.info("++++++++++++第一次修改");
                        }
                        log.info("修改车辆状态==={}", m);
                        payResultMapper.updateCar(m);
                        f = 1;
                    }

                } catch (Exception e) {
                    log.error("", e);
                }

//                    Map<String, String> parameMap = new HashMap<>();
//                    parameMap.put("openid", String.valueOf(userMap.get("openId")));
//                    parameMap.put("amt", String.valueOf(a));
//                    parameMap.put("amtCooperation", String.valueOf(newAmount));
//                    parameMap.put("licensePlateNumber", String.valueOf(carMap.get("licensePlateNumber")));
//                    parameMap.put("rechargeInfo", "主人，您已为您的爱车" + String.valueOf(carMap.get("licensePlateNumber")) + "充值成功。");
//                    parameMap.put("jump", "点击查看详情 >>");
//                    weiXinUtils.sendTemplate(17, parameMap);

                carMap.put("typeGuarantee", typeGuarantee);
                if (String.valueOf(carMap.get("status")).equals("30")) {
                    //已退出的车辆 且不是59包年活动
                    log.info("已退出的车辆，重新设置定时器===>");
                    addFoundationShowCustomer();
                    Map dicMap = new HashMap();
                    dicMap.put("type", "observationTime");
                    Map dictionary = payResultMapper.findDictionarySingle(dicMap);

                    Long imestamp = new Date().getTime();
                    imestamp = imestamp + Long.valueOf(dictionary.get("value").toString());
                    Date date1 = new Date(imestamp);
                    Map<String, Object> parameterMap = new HashMap<>();
                    SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String observationEndTime = dateTime.format(date1);
                    parameterMap.clear();
                    parameterMap.put("observationEndTime", observationEndTime);
                    parameterMap.put("id", carId);
                    parameterMap.put("reJoinNum", "reJoinNum");
                    parameterMap.put("signoutMessageFlag", "0");
                    log.info("更新观察期结束时间");
                    payResultMapper.updateCar(parameterMap);
                }
                if (f == 0) {
                    log.info("f = 0 未包年==========》");
                    Map<String, Object> rmap = new HashMap<>();
                    log.info("+++++++++进入新充值结果+++++++++++++++");
                    if (oldStatus.equals("1")) {
                        rmap.put("reJoinNum", "reJoinNum");
                    }
                    rmap.put("amtCooperation", newAmount);
                    rmap.put("status", status);
                    rmap.put("typeGuarantee", a.compareTo(new BigDecimal("99")) == 0 ? 2 : 1);
                    rmap.put("timeSignout", "1111-11-11 11:11:11");
                    rmap.put("reasonSignout", "");
                    rmap.put("signoutMessageFlag", "0");
                    rmap.put("id", carId);
                    rmap.put("payTime", date);
                    payResultMapper.updateCar(rmap);
                }
                updateDayNumber("payNum", 1); //更新支付数量
                if (sum.subtract(a).compareTo(new BigDecimal("0")) <= 0
                        && !oldStatus.equals("20") && !oldStatus.equals("13") && !oldStatus.equals("30")) {
                    log.info("首充创建观察期定时器====》");
                    observation(carMap);
                }
                if (oldStatus.equals("30")) {
                    // 新车充值59包年，创建观察期定时器
                    carMap.put("inviterAmount", a);
                    log.info("新车充值59包年，创建观察期定时器====》");
                    observation(carMap);
                }
            }
        }
        return 1;
    }

    public void updateDayNumber(String key, Integer number) {
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd");
        String day = time.format(new Date());
        Map<String, Object> pMap = new HashMap<>();
        pMap.put("day", day);
        List<Map<String, Object>> list = payResultMapper.findMore(pMap);
        if (list != null && list.size() > 0) {
            Map<String, Object> map = list.get(0);
            String data = String.valueOf(map.get(key));
            int num = data.equals("null") || data.equals("") ? 0 : Integer.valueOf(data);
            num = num + number;
            pMap.clear();
            pMap.put(key, num);
            pMap.put("id", map.get("id"));
            payResultMapper.wechatLoginUpdateModel(pMap);
        } else {
            pMap.clear();
            pMap.put(key, number);
            pMap.put("day", day);
            payResultMapper.wechatLoginSaveSingle(pMap);
        }
    }

    private void addFoundationShowCustomer() {
        log.info("增加人数方法进来了-----------------------");
        Map<String, Object> foundation = payResultMapper.findEntitySingle();
        Map<String, Object> paramModelMap = new HashMap<>();
        paramModelMap.put("showCustomer", (Integer) foundation.get("showCustomer") + 1);
        paramModelMap.put("versions", foundation.get("versions"));
        Integer integer = payResultMapper.updateData(paramModelMap);
        if (integer == 0)
            addFoundationShowCustomer();
        else
            log.info("数据修改成功");
        return;
    }

    public void observation(Map<String, Object> carMap) throws SchedulerException {
        log.info("{}车辆创建进入保障中定时器设置+++++++++++++++++++++++++++++++++", carMap);
        //创建定时任务，指定时间后进入保障期
        String carId = String.valueOf(carMap.get("id"));
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = time.format(new Date());
        Long imestamp = new Date().getTime();

        Map<String, Object> map = new HashMap<>();
        map.put("jobName", "observationJob_" + carId);
        map.put("carId", carId);
        map.put("typeGuarantee", carMap.get("typeGuarantee"));
        map.put("openid", carMap.get("openId"));
        Scheduler sche = schedulerFactory.getScheduler();
        removeJob(sche, map.get("jobName").toString());
        //从数据库查询定时时间
        Map dicMap = new HashMap();
        dicMap.put("type", "observationTime");
        Map dictionary = payResultMapper.findDictionarySingle(dicMap);
        //开启定时器
        Map<String, String> dateMap = DateUtil.getDateMap(new Long(dictionary.get("value").toString()));

        imestamp = imestamp + Long.valueOf(dictionary.get("value").toString());
        Date date1 = new Date(imestamp);
        String observationEndTime = time.format(date1);
        String cron = dateMap.get("second") + " " + dateMap.get("minute") + " " + dateMap.get("hour") + " " + dateMap.get("day") + " " + dateMap.get("month") + " ? *";
        map.put("cron", cron);
        map.put("yearFlag", carMap.get("yearFlag"));
        addJob(sche, map.get("jobName").toString(), ObservationJob.class, map, cron);

        // 车辆进入观察期，用户升级V1
        try {
            map.clear();
            Integer customerId = Integer.valueOf(carMap.get("customerId").toString());
            map = payResultMapper.getUserInfoById(customerId);
            if (!CollectionUtils.isEmpty(map) && 0 == Integer.valueOf(map.get("level").toString())) {
                map.clear();
                map.put("id", customerId);
                map.put("level", 1);
                payResultMapper.userUpdateModel(map);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        updateDayNumber("observationNum", 1);

        map.clear();
        map.put("observationEndTime", observationEndTime);
        map.put("id", carId);
        //进入观察期次数(传参数即可，sql自加)
        payResultMapper.updateCar(map);

        try {
            // 没有openid就不发送微信推送
            if (null == carMap.get("openId") || "".equals(carMap.get("openId")))
                return;
            map.clear();
            map.put("id", carId);
            Map<String, Object> m = payResultMapper.findCar(map);
            BigDecimal day = new BigDecimal(dictionary.get("value").toString()).divide(new BigDecimal("86400000"), 0, RoundingMode.HALF_UP);
            Map<String, String> rmap = new HashMap<>();
            rmap.put("openid", String.valueOf(carMap.get("openId")));
            rmap.put("licensePlateNumber", String.valueOf(carMap.get("licensePlateNumber")));
            rmap.put("amtCompensation", String.valueOf(carMap.get("amtCompensation"))); //互助额度
            rmap.put("day", String.valueOf(day));
            if (null != carMap.get("inviterAmount")) {
                rmap.put("money", String.valueOf(carMap.get("inviterAmount")));
            } else {
                rmap.put("money", m.get("amtCooperation").toString());
            }
//            weiXinUtils.sendTemplate(11, rmap);
            log.info("车辆创建进入保障中定时器创建成功+++++++++++++++++++++++++++++++++");
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void removeJob(Scheduler sche, String jobClassName) throws SchedulerException {
        Scheduler scheduler2 = schedulerFactory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, TRIGGER_GROUP_NAME);
        scheduler2.pauseTrigger(triggerKey); // 停止触发器
        boolean b = scheduler2.unscheduleJob(triggerKey); // 移除触发器
        log.debug("移除触发器={}", b);
        b = scheduler2.deleteJob(JobKey.jobKey(jobClassName, JOB_GROUP_NAME)); // 删除任务
        log.debug("删除任务={}", b);
    }

    public static void addJob(Scheduler sched, String jobName, @SuppressWarnings("rawtypes") Class cls, Object params,
                              String time) {
        try {
            log.info("{}车辆创建进入保障中定时器创建{}+++++++++++++++++++++++++++++++++", params, time);
            JobKey jobKey = new JobKey(jobName, JOB_GROUP_NAME);// 任务名，任务组，任务执行类

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("params", params);
            JobDetail jobDetail = newJob(cls).withIdentity(jobKey).setJobData(jobDataMap).build();
            TriggerKey triggerKey = new TriggerKey(jobName, TRIGGER_GROUP_NAME);// 触发器

            Trigger trigger = newTrigger().withIdentity(triggerKey).withSchedule(cronSchedule(time)).build();// 触发器时间设定
            sched.scheduleJob(jobDetail, trigger);
            if (!sched.isShutdown()) {
                sched.start();// 启动
            }
            log.info("+++++++++++++++++++车辆创建进入保障中定时器设置成功++++++++++++++++");
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}
