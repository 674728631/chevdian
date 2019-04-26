package com.cvd.chevdian.mapper.system;

import com.cvd.chevdian.bean.system.City;
import com.cvd.chevdian.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @author xiaowuge
 * @date 2019-03-28 17:13
 */
public interface CityMapper extends BaseMapper {

    /**
     * 根据省份id查询子城市
     */
    List<City> selectByProvinceId(Integer provinceId);

}
