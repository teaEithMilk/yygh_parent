package com.tu.yygh.hosp.controller;


import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.hosp.service.HospitalSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("获取所有医院信息")
    @GetMapping("/getAll")
    public Result getall(){
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Long id){
        boolean b = hospitalSetService.removeById(id);
        if(b){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    //条件查询分页
    @GetMapping("/findPageHospSet/{current}/{limit}")
    public Result findPageHospSet(
            @PathVariable Long current,
            @PathVariable Long limit,
            HospitalSetQueryVo hospitalSetQueryVo){
        Page<HospitalSet> page = new Page<>(current, limit);

        LambdaQueryWrapper<HospitalSet> queryWrapper = new LambdaQueryWrapper<>();

        //医院名称
        String hospName = hospitalSetQueryVo.getHosname();

        //医院编号
        String hoscode = hospitalSetQueryVo.getHoscode();

        //编写条件：模糊查询医院名称
        queryWrapper.like(StringUtils.isNotBlank(hospName),HospitalSet::getHosname,hospName);

        //编写条件：医院编号模糊查询
        queryWrapper.like(StringUtils.isNotBlank(hoscode),HospitalSet::getHoscode,hoscode);

        //查询
        Page<HospitalSet> hospPage = hospitalSetService.page(page, queryWrapper);

        return Result.ok(hospPage);
    }


}
