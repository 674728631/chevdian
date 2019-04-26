package com.cvd.chevdian.mapper;

import java.util.List;
import java.util.Map;

public interface RecordRechargeMapper {

    /**
     * 第一次充值时间
     */
    Map<String, Object> selectCreatTime(Map<String, Object> map);

    /**
     * 年度充值金额
     */
    Map<String, Object> selectAmt(Map<String, Object> map);

    /**
     * 保存充值记录
     */
    int saveSingle(Map<String, Object> map);

    /**
     * 更新充值记录
     */
    int updateModel(Map<String, Object> map);

    /**
     * 根据订单号查询
     */
    List<Map<String, Object>> slectByEventNo(String eventNo);
}
