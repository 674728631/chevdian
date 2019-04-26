package com.cvd.chevdian.mapper.system;

import com.cvd.chevdian.bean.system.OperationLogDto;

import java.util.List;

public interface OperationLogMapper {

    /**
     * 保存日志
     */
    int saveLog(OperationLogDto log);

    /**
     * 查询日志
     */
    List<OperationLogDto> getLog();
}
