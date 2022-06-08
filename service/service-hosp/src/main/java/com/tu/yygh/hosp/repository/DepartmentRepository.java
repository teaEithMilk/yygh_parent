package com.tu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * 科室
 * */

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {


    //继承了MongoRepository，只要遵守方法名规则，不需要实现方法
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

}
