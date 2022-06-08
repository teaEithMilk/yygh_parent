package com.tu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HospitalRepository extends MongoRepository<Hospital,String> {

    //继承了MongoRepository，只要遵守方法名规则，不需要实现方法
    Hospital getHospitalByHoscode(String hoscode);

}
