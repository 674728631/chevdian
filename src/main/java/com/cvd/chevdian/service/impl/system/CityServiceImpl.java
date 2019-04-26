package com.cvd.chevdian.service.impl.system;

import com.cvd.chevdian.bean.system.City;
import com.cvd.chevdian.mapper.system.CityMapper;
import com.cvd.chevdian.service.system.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaowuge
 * @date 2019-03-28 17:24
 */
@Service
public class CityServiceImpl implements CityService {

    @Autowired
    private CityMapper cityMapper;

    @Override
    public List<City> listCity() {
        // 查询省份
        List<City> provinces = cityMapper.selectByProvinceId(0);
        // 查询城市
        provinces.forEach(c -> c.setCities(cityMapper.selectByProvinceId(c.getId())));
        return provinces;
    }
}
