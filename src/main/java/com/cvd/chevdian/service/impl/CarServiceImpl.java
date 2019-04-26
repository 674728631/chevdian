package com.cvd.chevdian.service.impl;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.common.util.FileUtil;
import com.cvd.chevdian.common.util.WeiXinUtils;
import com.cvd.chevdian.mapper.CarMapper;
import com.cvd.chevdian.mapper.MaintenanceshopMapper;
import com.cvd.chevdian.mapper.RecordRechargeMapper;
import com.cvd.chevdian.mapper.UserDistributorMapper;
import com.cvd.chevdian.service.CarService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Slf4j
@Service
public class CarServiceImpl implements CarService {

    @Autowired
    WeiXinUtils weiXinUtils;
    @Autowired
    CarMapper carMapper;
    @Autowired
    MaintenanceshopMapper maintenanceshopMapper;
    @Autowired
    UserDistributorMapper userDistributorMapper;
    @Autowired
    AsyncTaskExecutor taskExecutor;
    @Autowired
    RecordRechargeMapper recordRechargeMapper;

    @Value("${chevhuzhu.url}")
    private String chevhuzhu;

    @Override
    public PageInfo<Car> getCarUserByDifferentUserType(UserDistributor user) {
        PageInfo<Car> pageInfo;
        ScreenVo screenVo = new ScreenVo();
        // 判断端用户角色
        if (user.getUserType() == DBDict.USER_TYPE_ADMIN) {
            // 管理员查看所有分销用户
            List<Car> rsCarList = new ArrayList<>();
            // 所有渠道
            List<UserDistributor> channelList = userDistributorMapper.selectListByType(user, DBDict.USER_TYPE_CHANNEL, screenVo);
            for (UserDistributor channel : channelList) {
                // 渠道的用户
                List<Car> carList = carMapper.getAllUserByShopId(channel.getChannelId(), new ScreenVo());
                rsCarList.addAll(carList);
                // 渠道的代理
                List<UserDistributor> agentList = userDistributorMapper.selectListByType(channel, DBDict.USER_TYPE_AGENT, screenVo);
                for (UserDistributor agent : agentList) {
                    // 代理的用户
                    List<Car> temp = carMapper.getAllUserByShopId(agent.getChannelId(), new ScreenVo());
                    rsCarList.addAll(temp);
                }
            }
            pageInfo = new PageInfo<>(rsCarList);
        } else {
            // 渠道/代理
            pageInfo = PageHelper.startPage(user.getPageNo(), user.getPageSize())
                    .doSelectPageInfo(() -> carMapper.getAllUserByShopId(user.getChannelId(), new ScreenVo()));
        }
        return pageInfo;
    }

    @Override
    public Car showCarDetail(Integer carId) {
        Car car = carMapper.getCarById(carId);
        if (null != car) {
            Car city = carMapper.getCarCity(carId);
            if (null != city) {
                car.setCityName(city.getCityName());
                car.setProvinceId(city.getProvinceId());
            }
            Car insurance = carMapper.getCarInsurance(carId);
            if (null != insurance) {
                car.setInsuranceId(insurance.getInsuranceId());
                car.setInsurance(insurance.getInsurance());
                car.setEndTime(insurance.getEndTime());
                car.setIsAutoRemind(insurance.getIsAutoRemind());
            }
            car.setRechargeAmt(carMapper.getCarRecharge(carId).getRechargeAmt());
        }
        return car;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyCarInfo(Car carInfo) {
        // 更新车主姓名
        int i = carMapper.modifyCarOwner(carInfo);

        Car insurance = carMapper.getCarInsurance(carInfo.getCarId());
        int j;
        if (null != insurance)
            // 更新保险信息
            j = carMapper.modifyCarInsurance(carInfo);
        else
            // 保存保险信息
            j = carMapper.saveCarInsurance(carInfo);
        if (i == 0 || j == 0)
            throw new BusinessException(ErrorCodeEnum.GL99990108);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDistributor getInviteQRcode(UserDistributor user) throws Exception {
//        // 1. 判断是代理还是渠道
//        if (10 == user.getUserType()) {
//            // 获取渠道邀请用户二维码
//            if (StringUtils.isEmpty(user.getUserQRcode())) {
//                ChannelVo channel = maintenanceshopMapper.queryById(user.getChannelId());
//                if (null == channel) {
//                    log.error("根据渠道id={}查询车V互助渠道失败", user.getChannelId());
//                    throw new BusinessException(ErrorCodeEnum.GL99990100);
//                }
//                String userQR = channel.getQrcode();
//                user.setUserQRcode(userQR);
//                // 保存地址
//                UserDistributor newUser = new UserDistributor(user.getId(), user.getUserQRcode());
//                userDistributorMapper.updateEntityModel(newUser);
//            }
//        } else {
//            // 2. 获取代理邀请用户二维码（车v互助）
//            if (StringUtils.isEmpty(user.getUserQRcode())) {
//                // 2.1 获取车v互助
//                JSONObject json = new JSONObject();
//                json.put("mima", java.util.Base64.getEncoder().encodeToString("R7&kf1".getBytes("utf-8")));
//                log.info("json = {}", json);
//                String rs = HttpUtil.doPostJson(chevhuzhu + "/api/accesstoken", json.toJSONString());
//                json = JSONObject.parseObject(rs);
//                if ("200".equals(json.getString("code"))) {
//                    String eventKey = String.format("%s_a", user.getId()); //TODO 格式待确认
//                    String fileName = weiXinUtils.createTempQrcode(eventKey, json.getString("data"));
//                    user.setUserQRcode(fileName);
//                    // 保存地址
//                    UserDistributor newUser = new UserDistributor(user.getId(), user.getUserQRcode());
//                    userDistributorMapper.updateEntityModel(newUser);
//                }
//            }
//        }

        // 获取file的oss地址
        String url = FileUtil.getImgURLFromOSS(GlobalConstant.OSS_CAR_IMG_URL, user.getUserQRcode());
        user.setUserQRcode(url);
        return user;
    }

    @Override
    public List<Map<String, Object>> getInsuranceList() {
        return carMapper.getInsuranceList();
    }

    @Override
    public PageInfo<Car> getRechargeList(UserDistributor loginUser) {
        List<Integer> shopIdList = new ArrayList<>();
        if (DBDict.USER_TYPE_ADMIN == loginUser.getUserType()) {
            // 查看渠道/代理的可充值用户
            List<UserDistributor> channelList = userDistributorMapper.selectListByType(loginUser, DBDict.USER_TYPE_CHANNEL, null);
            channelList.forEach(u -> shopIdList.add(u.getChannelId()));
            List<UserDistributor> agentList = userDistributorMapper.selectListByType(loginUser, DBDict.USER_TYPE_AGENT, null);
            agentList.forEach(u -> shopIdList.add(u.getChannelId()));
        } else {
            // 自己的可充值用户
            shopIdList.add(loginUser.getChannelId());
        }
//        pageInfo = PageHelper.startPage(loginUser.getPageNo(), loginUser.getPageSize())
//                .doSelectPageInfo(() -> carMapper.getValidRechargeCars(shopIdList));
        PageHelper.startPage(loginUser.getPageNo(), loginUser.getPageSize());
        List<Car> cars = carMapper.getValidRechargeCars(shopIdList);
        List<Future<Car>> futureList = new ArrayList<>();
        cars.forEach(car -> {
            Future<Car> f = taskExecutor.submit(() -> {
                Map<String, Object> rs = getPayAmount(car.getCarId().toString());
                car.setAmount((BigDecimal) rs.get("amount"));
                return car;
            });
            futureList.add(f);
        });
        futureList.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                log.error("",e);
                throw new RuntimeException("多线程处理充值记录异常");
            }
        });
        return new PageInfo<>(cars);
    }

    @Override
    public Map<String, Object> getPayAmount(String carId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        BigDecimal a = new BigDecimal("0");
        Map<String, Object> pmap = new HashMap<>();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        pmap.put("carId", carId);
        Map<String, Object> cMap = recordRechargeMapper.selectCreatTime(pmap);
        if (cMap != null) {
            String ctime = String.valueOf(cMap.get("ctime")); // 第一次充值时间（包括系统充值）
            String rId = String.valueOf(cMap.get("id"));
            Calendar nTime = Calendar.getInstance();
            Calendar cTime = Calendar.getInstance();

            cTime.setTime(time.parse(ctime));
            for (int i = 0; i < 100; i++) {
                cTime.add(Calendar.YEAR, 1);
                if (cTime.getTime().getTime() > nTime.getTime().getTime()) { // 满一年了，计算新的一年可充值金额
                    cTime.add(Calendar.YEAR, -1);
                    pmap.put("time", time.format(cTime.getTime()));
                    Map<String, Object> sMap = recordRechargeMapper.selectAmt(pmap);
                    if (sMap != null) {
                        a = new BigDecimal(String.valueOf(sMap.get("sum")).equals("null") ? "0" : String.valueOf(sMap.get("sum")));
                        map.put("minT", sMap.get("minT"));
                    }
                    break;
                }
            }
            map.put("rId", rId);
        }
        map.put("amount", a);
        map.put("carId", carId);
        return map;
    }

    /**
     * 充值金额选项
     */
    private List<Map<String, Object>> rechargeAmtList() {
        List<Map<String, Object>> mList = new ArrayList<>();
        Map<String, Object> mMap = new HashMap<>();
        mMap.put("id", "1");
        mMap.put("amount", 9);
        mMap.put("remark", "体验价，预计分摊1个月");
        mList.add(mMap);

        mMap = new HashMap<>();
        mMap.put("id", "3");
        mMap.put("amount", 99);
        mMap.put("remark", "分摊全年12个月");
        mList.add(mMap);

        // 充值29
        mMap = new HashMap<>();
        mMap.put("id", "4");
        mMap.put("amount", 29);
        mMap.put("remark", "预计分摊3个月");
        mList.add(mMap);

        // 充值59
        mMap = new HashMap<>();
        mMap.put("id", "5");
        mMap.put("amount", 59);
        mMap.put("remark", "预计分摊3个月");
        mList.add(mMap);
        return mList;
    }

}
