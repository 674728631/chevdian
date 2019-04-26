package com.cvd.chevdian.service.task;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.Car;
import com.cvd.chevdian.bean.vo.ScreenVo;
import com.cvd.chevdian.mapper.CarMapper;
import com.cvd.chevdian.service.impl.StatisticalDataServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class CarListForkJoinTask extends RecursiveTask<List<Car>> {

    private List<UserDistributor> agentList;

    private CarMapper carMapper;

    private StatisticalDataServiceImpl statisticalDataService;

    private ScreenVo screenVo;

    public CarListForkJoinTask(List<UserDistributor> agentList, CarMapper carMapper, StatisticalDataServiceImpl statisticalDataService, ScreenVo screenVo) {
        this.agentList = agentList;
        this.carMapper = carMapper;
        this.statisticalDataService = statisticalDataService;
        this.screenVo = screenVo;
    }

    @Override
    public List<Car> compute() {
        if (agentList.size() == 0)
            return new ArrayList<>();
        List<Car> rsCarList = null;
        List<CarListForkJoinTask> subTasks = new ArrayList<>();
        if (agentList.size() > 2) {
            List<UserDistributor> subCars = new ArrayList<>();
            for (UserDistributor user : agentList) {
                subCars.add(user);
                subTasks.add(new CarListForkJoinTask(subCars, carMapper, statisticalDataService, screenVo));
                subCars.clear();
            }
        } else {
            rsCarList = new ArrayList<>();
            rsCarList = execute(agentList.get(0), rsCarList);
        }
        if (!subTasks.isEmpty()) {
            rsCarList = new ArrayList<>();
            for (CarListForkJoinTask subTask : invokeAll(subTasks)) {
                List<Car> carList = subTask.join();
                rsCarList.addAll(carList);
            }
        }
        return rsCarList;
    }

    public List<Car> execute(UserDistributor user, List<Car> rsCarList) {
        // 自己的一级用户
        List<Car> carList = carMapper.getAllUserByShopId(user.getChannelId(), screenVo);
        // 一级用户的邀请用户
        rsCarList = statisticalDataService.getCarList(carList, rsCarList, screenVo);
        return rsCarList;
    }
}

