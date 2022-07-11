package com.tu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface HospitalSetService extends IService<HospitalSet> {

    //查询医院签名
    String getSignKey(String hoscode);

    //获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);

}

