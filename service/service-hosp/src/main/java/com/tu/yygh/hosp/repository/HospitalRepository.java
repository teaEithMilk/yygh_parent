package com.tu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HospitalRepository extends MongoRepository<Hospital,String> {

    //继承了MongoRepository，只要遵守方法名规则，不需要实现方法
    Hospital getHospitalByHoscode(String hoscode);


    /**
     * 根据医院名称获取医院列表
     */
    List<Hospital> findHospitalByHosnameLike(String hosname);

}
