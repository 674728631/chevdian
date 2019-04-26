package com.cvd.chevdian.mapper;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.vo.ChannelVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MaintenanceshopMapper extends BaseMapper {

    /**
     * 根据id查询
     */
    ChannelVo queryById(Integer id);

    /**
     * 根据name进行模糊查询
     */
    List<ChannelVo> selectLikeName(@Param("name") String name);

    /**
     * 根据name查询详情
     */
    List<ChannelVo> queryByName(String name);

    /**
     * 更新为分销渠道
     */
    int updateFlagById(ChannelVo user);

    /**
     * 更新渠道
     */
    int updateById(UserDistributor user);
}
