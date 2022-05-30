package com.tu.yygh.hosp.config;


import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.tu.yygh.hosp.service.HospitalSetService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@MapperScan("com.tu.yygh.hosp.mapper")
@Configuration
public class HospConfig {
    /**
     * 分页插件
     * */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }

}
