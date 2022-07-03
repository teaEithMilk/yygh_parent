package com.tu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
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

    /**
     * 更新医院上线状态
     * */
    void updateStatus(String id, Integer status);


    /**
     * 根据医院ID获取医院详情
     * */
    Map<String, Object> showHospDetail(String id);


    /**
     * 根据医院名称获取医院列表
     */
    List<Hospital> findByHosname(String hosname);

    /**
     * 根据医院编号获取医院详情信息
     * */
    Map<String, Object> findByHoscodeGetHospDetail(String hoscode);

    /**
     * 根据医院编号获取医院名称
     * */
    String getHospName(String hoscode);
}
