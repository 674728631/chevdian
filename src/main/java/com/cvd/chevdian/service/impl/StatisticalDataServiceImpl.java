package com.cvd.chevdian.service.impl;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.enums.ErrorCodeEnum;
import com.cvd.chevdian.common.exception.BusinessException;
import com.cvd.chevdian.mapper.CarMapper;
import com.cvd.chevdian.mapper.UserDistributorMapper;
import com.cvd.chevdian.service.StatisticLambda;
import com.cvd.chevdian.service.StatisticalDataService;
import com.cvd.chevdian.service.UserDistributorService;
import com.cvd.chevdian.service.task.CarListForkJoinTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
public class StatisticalDataServiceImpl implements StatisticalDataService {

    @Autowired
    UserDistributorService userDistributorService;
    @Autowired
    CarMapper carMapper;
    @Autowired
    UserDistributorMapper userDistributorMapper;
    @Autowired
    StatisticalDataServiceImpl statisticalDataService;

    @Override
    public Integer getChannelCount(ScreenVo screenVo) {
        endTimeHandle(screenVo);
        return userDistributorService.channelCount(screenVo);
    }

    @Override
    public Integer getAgentCountByUserType(UserDistributor user, ScreenVo screenVo) {
        endTimeHandle(screenVo);
        return userDistributorService.selectAgentCountByUserType(user, screenVo);
    }

    @Override
    public List<Car> getCarListByUserType(UserDistributor user, ScreenVo screenVo) {
        endTimeHandle(screenVo);
        List<Car> rsCarList = new ArrayList<>();
        // 渠道 -- 自己的用户+代理的用户+用户的用户的用户...
        if (user.getUserType() == DBDict.USER_TYPE_CHANNEL) {
            // 渠道的一级用户
            List<Car> carList = carMapper.getAllUserByShopId(user.getChannelId(), screenVo);
            // 一级用户的邀请用户
            rsCarList = getCarList(carList, rsCarList, screenVo);
            // 渠道所有代理
            List<UserDistributor> agentList = userDistributorMapper.selectListByType(user, DBDict.USER_TYPE_AGENT, screenVo);
            CarListForkJoinTask task = new CarListForkJoinTask(agentList, carMapper, statisticalDataService, screenVo);
            if (agentList.size() > 1) {
                List<Car> user1 = task.compute();
                rsCarList.addAll(user1);
            } else {
                List<Car> user2 = new ForkJoinPool().invoke(task);
                rsCarList.addAll(user2);
            }
        }
        // 代理 -- 自己的用户+用户的用户的用户...
        else if (user.getUserType() == DBDict.USER_TYPE_AGENT) {
            List<UserDistributor> agentList = new ArrayList<>();
            agentList.add(user);
            CarListForkJoinTask task = new CarListForkJoinTask(agentList, carMapper, statisticalDataService, screenVo);
            List<Car> user2 = new ForkJoinPool().invoke(task);
            rsCarList.addAll(user2);
        }
        // 管理员 -- 所有渠道--代理
        else if (user.getUserType() == DBDict.USER_TYPE_ADMIN) {
            // 所有渠道
            List<UserDistributor> channelList = userDistributorMapper.selectListByType(user, DBDict.USER_TYPE_CHANNEL, screenVo);
            for (UserDistributor channel : channelList) {
                List<Car> temp = getCarListByUserType(channel, screenVo);
                rsCarList.addAll(temp);
            }
        }
        return rsCarList;
    }

    @Override
    public String recharger(UserDistributor user, ScreenVo screenVo) {
        ScreenVo screenVo1 = new ScreenVo();
        screenVo1.setCityId(screenVo.getCityId());
        List<Car> carList = getCarListByUserType(user, screenVo1);
        List<Integer> carIdList = new ArrayList<>();
        for (Car car : carList) {
            carIdList.add(car.getCarId());
        }
        List<Integer> types = new ArrayList<>();
        types.add(DBDict.RECHARGE_EVENTTYPE_PAY);
        types.add(DBDict.RECHARGE_EVENTTYPE_RECHARGE);
        Map<String, Object> amt = carMapper.getAmtByType(types, carIdList, screenVo);
        BigDecimal cashAmt = new BigDecimal(amt.get("cashAmt").toString());
        BigDecimal rechargeAmt = new BigDecimal(amt.get("rechargeAmt").toString());
        return rechargeAmt.subtract(cashAmt).toString();
    }

    @Override
    public String refund(UserDistributor user, ScreenVo screenVo) {
        ScreenVo screenVo1 = new ScreenVo();
        screenVo1.setCityId(screenVo.getCityId());
        List<Car> carList = getCarListByUserType(user, screenVo1);
        List<Integer> carIdList = new ArrayList<>();
        for (Car car : carList) {
            carIdList.add(car.getCarId());
        }
        List<Integer> types = new ArrayList<>();
        types.add(DBDict.RECHARGE_EVENTTYPE_REFUND);
        Map<String, Object> amt = carMapper.getAmtByType(types, carIdList, screenVo);
        return amt.get("rechargeAmt").toString().startsWith("-") ? amt.get("rechargeAmt").toString().substring(1) : amt.get("rechargeAmt").toString();
    }

    @Override
    public List<Car> lookCarList(UserDistributor user, ScreenVo screenVo) {
        endTimeHandle(screenVo);
        // 渠道/代理
        List<Car> myList = null;
        if (user.getUserType() == DBDict.USER_TYPE_CHANNEL || user.getUserType() == DBDict.USER_TYPE_AGENT) {
            myList = carMapper.getAllUserByShopId(user.getChannelId(), screenVo);
        }
        // 管理员
        else if (user.getUserType() == DBDict.USER_TYPE_ADMIN) {
            myList = getCarListByUserType(user, screenVo);
        }
        return myList;
    }

    @Override
    public List<Map<String, Object>> getStatisticalByLetter(UserDistributor user, ScreenVo screenVo, Integer type, String letter) {
        endTimeHandle(screenVo);
        List<Map<String, Object>> rsList = new ArrayList<>();
        // 一级分销
        List<UserDistributor> userList = userDistributorMapper.selectUserByFirstPinYinChar(user, type, screenVo, letter);
        if (type == DBDict.USER_TYPE_AGENT) {
//            if (user.getUserType() == DBDict.USER_TYPE_ADMIN || user.getUserType() == DBDict.USER_TYPE_CHANNEL) {
//                // 管理员 -- 所有代理
//                List<UserDistributor> userList = userDistributorMapper.selectUserByFirstPinYinChar(user, type, screenVo, letter);
//                createReturnMap(userList, screenVo, rsList);
//            } else if (user.getUserType() == DBDict.USER_TYPE_AGENT) {
//                // 代理 -- 我的、其他
//                List<UserDistributor> userList = new ArrayList<>();
//                userList.add(user);
//                createReturnMap(userList, screenVo, rsList);
//            }
            // 处理返回结果
            // 根据分销查询互助用户
            rsList = createReturnMap(userList, screenVo, this::agentLookCarList, user);
        } else if (type == DBDict.USER_TYPE_CHANNEL) {
//            if (user.getUserType() == DBDict.USER_TYPE_CHANNEL) {
//                // 渠道 -- 我的、其他
//               Map<String,Object> rsMap = channelLookCarList(user, screenVo);
//                if (agentMap.size() > 0) {
//                    Map<String, Object> temp = new HashMap<>();
//                    temp.put("name", agentMap.get("name"));
//                    temp.put("myNum", agentMap.get("myNum"));
//                    temp.put("id", agentMap.get("id"));
//                    othersNum += Integer.valueOf(agentMap.get("othersNum").toString());
//                    rsList.add(temp);
//                }
//                Map<String, Object> temp2 = new HashMap<>();
//                temp2.put("name", "others");
//                temp2.put("id", -1);
//                temp2.put("myNum", othersNum);
//                rsList.add(temp2);
//            } else if (user.getUserType() == DBDict.USER_TYPE_ADMIN) {
//                // 管理员 -- 所有渠道
//                List<UserDistributor> userList = userDistributorMapper.selectUserByFirstPinYinChar(user, type, screenVo, letter);
//                for (UserDistributor channel : userList) {
//                    Map<String, Object> agentMap = channelLookCarList(channel, screenVo);
//                    rsList.add(agentMap);
//                }
//                rsMap.put("channelList", rsList);
//            }
            // 处理返回结果
            rsList = createReturnMap(userList, screenVo, this::channelLookCarList, user);
        }
        return rsList;
    }

    @Override
    public List<Car> lookCarListById(UserDistributor loginUser, ScreenVo screenVo, Integer type, Integer id) {
        // 判断其他按钮--> id=-1
        if (-1 == id) {
            if (DBDict.USER_TYPE_ADMIN != loginUser.getUserType())
                throw new BusinessException(ErrorCodeEnum.GL99990107);
            List<Car> rsList = new ArrayList<>();
            List<UserDistributor> userList = userDistributorMapper.selectUserByFirstPinYinChar(loginUser, type, screenVo, null);
            if (DBDict.USER_TYPE_CHANNEL == type) {
                // 渠道的其他
                userList.forEach(user -> {
                    Map<String, Object> temp = channelLookCarList(user, screenVo);
                    rsList.addAll((List<Car>) temp.get("othersList"));
                });
            } else if (DBDict.USER_TYPE_AGENT == type) {
                // 代理的其他
                userList.forEach(user -> {
                    Map<String, Object> temp = agentLookCarList(user, screenVo);
                    rsList.addAll((List<Car>) temp.get("othersList"));
                });
            }
            return rsList;
        }
        // 根据id判断查看渠道
        UserDistributor lookUser = userDistributorMapper.queryById(id);
        if (null == lookUser || null == lookUser.getUserType())
            throw new IllegalArgumentException("查看对象不存在");
        Integer userType = lookUser.getUserType();
        // 判断权限
        isInvalidToLookCarListById(lookUser, loginUser);

        Map<String, Object> rsMap = new HashMap<>();
        if (userType == DBDict.USER_TYPE_AGENT) {
            rsMap = agentLookCarList(lookUser, screenVo);
        } else if (userType == DBDict.USER_TYPE_CHANNEL) {
            rsMap = channelLookCarList(lookUser, screenVo);
        }
        List<Car> rsList = new ArrayList<>();
        if (null != rsMap) {
            rsList.addAll((List<Car>) rsMap.get("myCarList"));
            if (loginUser.getUserType() == DBDict.USER_TYPE_ADMIN) {
                // 管理员点击代理 -- 代理的所有（我的+其他）
                // 代理点击代理 -- 代理的一级互助用户（我的）
                rsList.addAll((List<Car>) rsMap.get("othersList"));
            }
        }
        return rsList;
    }

    /**
     * 用户邀请的用户(无限级)
     */
    public List<Car> getCarList(List<Car> carList, List<Car> othersList, ScreenVo screenVo) {
//        rsCarList.addAll(carList);
//        if (carList.size() > 0) {
//            Set<Integer> customerIdList = new HashSet<>();
//            // 用户id，去重
//            for (Car car : carList) {
//                customerIdList.add(car.getCustomerId());
//            }
////            for (Car car : carList) {
////                boolean time = true;
////                boolean city = true;
////                if (screenVo.getBeginTime() != null && screenVo.getEndTime() != null
////                        && !(car.getCreateAt().compareTo(screenVo.getBeginTime()) >= 0
////                        && car.getCreateAt().compareTo(screenVo.getEndTime()) <= 0)) {
////                    time = false;
////                }
////                if (screenVo.getCityId() != null
////                        && screenVo.getCityId() != car.getCityId()) {
////                    city = false;
////                }
////                if (time && city) {
////                    rsCarList.add(car);
////                    customerIdList.add(car.getCustomerId());
////                }
////            }
//            // 用户的用户
//            carList = carMapper.getInvitedUserOfUser(customerIdList, screenVo);
//            getCarList(carList, rsCarList, screenVo);
//        }
//        return rsCarList;
        if (carList.size() > 0) {
            othersList.addAll(carList);
            Set<Integer> customerIdList = new HashSet<>();
            // 用户id，去重
            for (Car car : carList) {
                customerIdList.add(car.getCustomerId());
            }
            carList = carMapper.getInvitedUserOfUser(customerIdList, screenVo);

            getCarList(carList, othersList, screenVo);
        }
        return othersList;
    }

    /**
     * 用户邀请的用户
     */
    private List<Car> invitedCarList(List<Car> carList, ScreenVo screenVo) {
//        if (carList.size() > 0) {
//            Set<Integer> customerIdList = new HashSet<>();
//            // 用户id，去重
//            for (Car car : carList) {
//                customerIdList.add(car.getCustomerId());
//            }
//            carList = carMapper.getInvitedUserOfUser(customerIdList, screenVo);
//        }
//        return carList;

        // 用户的邀请的邀请的...
        List<Car> temp = new ArrayList<>(carList);
        List<Car> othersList = new ArrayList<>();
        othersList = getCarList(temp, othersList, screenVo);
        return othersList;
    }

    /**
     * 代理查看自己一级用户
     * 我的数量，其他数量
     */
    private Map<String, Object> agentLookCarList(UserDistributor user, ScreenVo screenVo) {
        Map<String, Object> rsMap = new HashMap<>();
        // 一级用户
        List<Car> myCarList = carMapper.getAllUserByShopId(user.getChannelId(), screenVo);
        // 其他用户（一级用户的邀请用户）
        List<Car> othersList = invitedCarList(myCarList, screenVo);
        rsMap.put("myCarList", myCarList);
        rsMap.put("myNum", myCarList.size());
        rsMap.put("othersNum", othersList.size());
        rsMap.put("name", user.getUserName());
        rsMap.put("id", user.getId());
        rsMap.put("tel", user.getUserPn());
        rsMap.put("totalNum", (Integer) rsMap.get("myNum") + (Integer) rsMap.get("othersNum"));
        rsMap.put("othersList", othersList);
        return rsMap;
    }

    /**
     * 渠道查看自己的一级用户
     * 我的数量 = 一级互助用户
     * 其他数量 = 一级互助用户的邀请+各代理的所有
     */
    private Map<String, Object> channelLookCarList(UserDistributor user, ScreenVo screenVo) {
        Map<String, Object> rsMap = new HashMap<>();
        // 一级用户
        List<Car> myCarList = carMapper.getAllUserByShopId(user.getChannelId(), screenVo);
        rsMap.put("myCarList", myCarList);
        rsMap.put("myNum", myCarList.size());
        // 一级用户的邀请
        Integer othersNum = 0;
        List<Car> othersList = invitedCarList(myCarList, screenVo);
        othersNum += othersList.size();
        rsMap.put("othersList", othersList);
        // 渠道所有代理,代理的一级用户和邀请用户
        List<UserDistributor> agentList = userDistributorMapper.selectListByType(user, DBDict.USER_TYPE_AGENT, screenVo);
        List<Map<String, Object>> agentCarList = new ArrayList<>();

        for (UserDistributor userDistributor : agentList) {
            Map<String, Object> agentLookCarList = agentLookCarList(userDistributor, screenVo);
            if (null != agentLookCarList.get("totalNum")) {
                othersNum += Integer.valueOf(agentLookCarList.get("totalNum").toString());
                agentCarList.add(agentLookCarList);
                othersList.addAll((List<Car>) agentLookCarList.get("myCarList"));
                othersList.addAll((List<Car>) agentLookCarList.get("othersList"));
            }
        }
        rsMap.put("othersNum", othersNum);
        rsMap.put("agentList", agentCarList);
        rsMap.put("name", user.getUserName());
        rsMap.put("id", user.getId());
        rsMap.put("tel", user.getUserPn());
        rsMap.put("totalNum", (Integer) rsMap.get("myNum") + (Integer) rsMap.get("othersNum"));
        return rsMap;
    }

//    /**
//     * 管理员查看所有渠道
//     */
//    private Map<String, Object> adminLookChannelCarList(UserDistributor user, ScreenVo screenVo) {
//        Map<String, Object> rsMap = new HashMap<>();
//        List<Map<String, Object>> channelCarList = new ArrayList<>();
//        // 所有渠道
//        List<UserDistributor> channelList = userDistributorMapper.selectListByType(user, DBDict.USER_TYPE_CHANNEL, screenVo);
//        for (UserDistributor channel : channelList) {
//            Map<String, Object> channelMap = channelLookCarList(channel, screenVo);
//            channelCarList.add(channelMap);
//        }
//        rsMap.put("channelList", channelCarList);
//        return rsMap;
//    }
//
//    /**
//     * 管理员查看所有代理
//     */
//    public List<Map<String, Object>> adminLookAgentCarList(UserDistributor user, ScreenVo screenVo) {
//        List<Map<String, Object>> rsMap = new ArrayList<>();
//        // 所有代理
//        List<UserDistributor> agentList = userDistributorMapper.selectListByType(user, DBDict.USER_TYPE_AGENT, screenVo);
//        for (UserDistributor channel : agentList) {
//            Map<String, Object> channelMap = agentLookCarList(channel, screenVo);
//            rsMap.add(channelMap);
//        }
//        return rsMap;
//    }


    /**
     * 开始时间等于结束时间时，结束时间+1天
     */
    private void endTimeHandle(ScreenVo screenVo) {
        if (null != screenVo.getBeginTime()
                && null != screenVo.getEndTime()
                && screenVo.getBeginTime().compareTo(screenVo.getEndTime()) == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(screenVo.getBeginTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            screenVo.setEndTime(calendar.getTime());
        }
    }

    /**
     * 根据分销类型（代理/渠道） 查询互助用户数量（我的+其他）
     */
    private List<Map<String, Object>> createReturnMap(List<UserDistributor> userList, ScreenVo screenVo, StatisticLambda<UserDistributor, ScreenVo> statisticLambda, UserDistributor loginUser) {
        List<Map<String, Object>> rsList = new ArrayList<>();
        Integer othersNum = 0;
        for (UserDistributor agent : userList) {
            Map<String, Object> agentMap = statisticLambda.getOwnerAndOhtersCarList(agent, screenVo);
//            Map<String, Object> agentMap = agentLookCarList(agent, screenVo);
//            Map<String, Object> agentMap = channelLookCarList(agent, screenVo);
            if (agentMap.size() > 0) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("name", agentMap.get("name"));
                if (loginUser.getUserName().equals(agentMap.get("name")))
                    temp.put("name", agentMap.get("我的"));
                temp.put("num", agentMap.get("myNum"));
                temp.put("id", agentMap.get("id"));
                temp.put("tel", agentMap.get("tel"));
                othersNum += Integer.valueOf(agentMap.get("othersNum").toString());
                rsList.add(temp);
            }
        }
        Map<String, Object> temp2 = new HashMap<>();
        temp2.put("name", "其他");
        temp2.put("id", -1);
        temp2.put("num", othersNum);
        rsList.add(temp2);
        return rsList;
    }

    /**
     * 点击我的/其他/渠道/代理... 权限判断
     *
     * @param lookUser  查看类型
     * @param loginUser 登录用户
     */
    private void isInvalidToLookCarListById(UserDistributor lookUser, UserDistributor loginUser) {
        // 查看渠道
        boolean flag = true;
        if (DBDict.USER_TYPE_CHANNEL == lookUser.getUserType()) {
            // 管理员 -- 查看所有
            // 渠道 -- 我的
            // 代理 -- 无权
            if ((DBDict.USER_TYPE_CHANNEL == loginUser.getUserType() && lookUser.getId() != loginUser.getId())
                    || DBDict.USER_TYPE_AGENT == loginUser.getUserType())
                flag = false;
        }
        // 查看代理
        else if (DBDict.USER_TYPE_AGENT == lookUser.getUserType()) {
            // 管理员 -- 查看所有
            // 渠道 -- 无权
            // 代理 -- 我的
            if ((DBDict.USER_TYPE_AGENT == loginUser.getUserType() && lookUser.getId() != loginUser.getId())
                    || DBDict.USER_TYPE_CHANNEL == loginUser.getUserType())
                flag = false;
        } else
            throw new IllegalArgumentException("查看对象角色异常");
        if (!flag)
            throw new BusinessException(ErrorCodeEnum.GL99990107);
    }
}
