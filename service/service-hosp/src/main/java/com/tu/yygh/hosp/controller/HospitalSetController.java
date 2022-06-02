package com.tu.yygh.hosp.controller;


import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.common.utils.MD5;
import com.tu.yygh.hosp.service.HospitalSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//跨域注解
@CrossOrigin
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("获取所有医院信息")
    @GetMapping("/getAll")
    public Result getall(){
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }


    @ApiOperation("删除医院信息")
    @DeleteMapping("del/{id}")
    public Result delete(@PathVariable Long id){
        boolean b = hospitalSetService.removeById(id);
        if(b){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    //条件查询分页
    @ApiOperation("获取所有医院信息分页带条件")
    @PostMapping("/findPageHospSet/{current}/{limit}")
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

    /**
     * 添加医院设置
     * */
    @ApiOperation("保存医院信息")
    @PostMapping("/saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        //签名秘钥
        Random random = new Random();

        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));

        //调用service
        boolean save = hospitalSetService.save(hospitalSet);
        if(save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    /**
     * 根据id获取医院设置
     */
    @ApiOperation("根据id获取医院信息")
    @GetMapping("/getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }


    /**
     * 修改医院设置
     */
    @ApiOperation("修改医院信息")
    @PostMapping("/updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
    /**
     * 7 批量删除医院设置
     * */
    @ApiOperation("批量删除医院信息")
    @DeleteMapping("/batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    /**
     *医院设置锁定和解锁
     * */
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer status) {
        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        //调用方法
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }

    /**
     *  发送签名秘钥
     * */
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //TODO 发送短信
        return Result.ok();
    }
}
