package com.cvd.chevdian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cvd.chevdian.mapper")
public class ChevdianApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(ChevdianApplication.class, args);
    }

}
