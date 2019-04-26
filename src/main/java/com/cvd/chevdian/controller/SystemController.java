package com.cvd.chevdian.controller;


import com.cvd.chevdian.bean.system.OperationLogDto;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.mapper.system.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SystemController {

    @Autowired
    OperationLogMapper operationLogMapper;

    @GetMapping("getLog")
    public Wrapper<List<OperationLogDto>> getLog() {
        return WrapMapper.ok(operationLogMapper.getLog());
    }
}
