package com.tu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.model.hosp.Hospital;
import com.tu.yygh.hosp.repository.HospitalRepository;
import com.tu.yygh.hosp.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    /**
     * 查询医院
     * */
    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    /**
     * 上传医院
     * */
    @Override
    public void save(Map<String, Object> paramMap) {
        //把参数map集合转为实体类 Hospital,先转为json类型
        String mapString = JSONObject.toJSONString(paramMap);
        //传入实体类class，转为对象
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //判断是否存在数据,在hospitalRepository中继承了MongoRepository，只要遵守方法名规则，不需要实现方法
        Hospital hospitalExist  = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());

        //如果存在，进行修改
        if(hospitalExist != null){
            hospitalExist.setStatus(hospitalExist.getStatus());
            hospitalExist.setCreateTime(hospitalExist.getCreateTime());
            hospitalExist.setUpdateTime(new Date());
            hospitalExist.setIsDeleted(0);
            hospitalRepository.save(hospitalExist);
        }else{
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }

    }
}
