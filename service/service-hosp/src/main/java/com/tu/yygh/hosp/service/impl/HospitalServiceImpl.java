package com.tu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.tu.yygh.cmn.client.DictFeignClient;
import com.tu.yygh.hosp.repository.HospitalRepository;
import com.tu.yygh.hosp.service.HospitalService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    //远程调用feign
    @Autowired
    private DictFeignClient dictFeignClient;

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


    /**
     *  医院管理获取分页
     * */
    @Override
    public Page<Hospital> selectHosPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页
        Pageable pageable = PageRequest.of(page-1, limit, sort);
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        hospital.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        //获取数据，插入医院等级信息
        List<Hospital> content = pages.getContent();

        for (Hospital hos : content) {
            String hostypeName = dictFeignClient.getName("Hostype", hos.getHostype());
            hos.getParam().put("hostypeString",hostypeName);
            //获取省地区
            String provinceString = dictFeignClient.getName(hos.getProvinceCode());
            //获取市地区
            String cityString = dictFeignClient.getName(hos.getCityCode());
            //获取区地区
            String districtString = dictFeignClient.getName(hos.getDistrictCode());
            //封装地区
            hos.getParam().put("fullAddress", provinceString + cityString + districtString + hos.getAddress());
        }
        return pages;
    }

    /**
     * 更新医院上线状态
     * */
    @Override
    public void updateStatus(String id, Integer status) {
        //根据id获取对象
        Hospital hospital = hospitalRepository.findById(id).get();
        //修改状态值
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        //修改
        hospitalRepository.save(hospital);
    }

    /**
     * 根据医院ID获取医院详情信息
     * */
    @Override
    public Map<String, Object> showHospDetail(String id) {

        Map<String, Object> result = new HashMap<>();

        Hospital hospital = hospitalRepository.findById(id).get();

        result.put("hospital", hospital);

        result.put("bookingRule",hospital.getBookingRule());

        //获取省地区
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        //获取市地区
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        //获取区地区
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());
        //封装地区
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());

        hospital.setBookingRule(null);

        return result;
    }


    /**
     * 根据医院名称获取医院列表
     */
    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    /**
     * 根据医院编号获取医院详情信息
     * */
    @Override
    public Map<String, Object> findByHoscodeGetHospDetail(String hoscode) {
        //最终返回结果
        Map<String, Object> result = new HashMap<>();

        //根据医院编号查询医院
        Hospital hospital = this.getByHoscode(hoscode);

        //医院信息
        result.put("hospital", hospital);

        //封装预约规则
        result.put("bookingRule",hospital.getBookingRule());

        //不需要重复返回
        hospital.setBookingRule(null);

        return result;
    }


    /**
     * 根据医院编号获取医院名称
     * */
    @Override
    public String getHospName(String hoscode) {
        
        return null;
    }
}
