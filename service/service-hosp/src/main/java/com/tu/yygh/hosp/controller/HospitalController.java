package com.tu.yygh.hosp.controller;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.hosp.service.HospitalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//跨域注解
@CrossOrigin
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    /**
     *  医院管理获取分页
     * */
    @ApiOperation(value = "医院管理获取分页")
    @GetMapping("/list/{page}/{limit}")
    public Result selectHosPage(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitalsList =  hospitalService.selectHosPage(page,limit,hospitalQueryVo);
        return Result.ok(hospitalsList);
    }

    /**
     * 更新医院上线状态
     * */
    @ApiOperation(value = "更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result lock(@PathVariable("id") String id,@PathVariable("status") Integer status){
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    /**
     * 获取医院详情
     * */
    @ApiOperation(value = "获取医院详情")
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(@PathVariable String id){
        Map<String, Object> hospital =  hospitalService.showHospDetail(id);
        return Result.ok(hospital);
    }

}
