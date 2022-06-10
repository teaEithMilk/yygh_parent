package com.tu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getByHoscode(String hoscode);


    /**
     *
     *  医院管理获取分页
     *
     * */
    Page<Hospital> selectHosPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

}
