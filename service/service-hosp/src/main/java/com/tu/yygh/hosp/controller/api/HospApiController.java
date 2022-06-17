package com.tu.yygh.hosp.controller.api;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.hosp.service.DepartmentService;
import com.tu.yygh.hosp.service.HospitalService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
public class HospApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@PathVariable Integer page,
                        @PathVariable Integer limit,
                        HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectHosPage(page, limit, hospitalQueryVo);

        return Result.ok(hospitals);
    }

    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("/findByHosname/{hosname}")
    public Result findByHosname(
            @ApiParam(name = "hosname", value = "医院名称", required = true)
            @PathVariable String hosname){
        return Result.ok(hospitalService.findByHosname(hosname));
    }

    @ApiOperation(value = "根据医院编号获取医院科室")
    @GetMapping("/findByHoscodeGetdepartment/{hoscode}")
    public Result findByHoscodeGetdepartment(@PathVariable String hoscode){
        return Result.ok(departmentService.findDeptTree(hoscode));
    }

    @ApiOperation(value = "根据医院编号获取医院详情信息")
    @GetMapping("/findByHoscodeGetHospDetail/{hoscode}")
    public Result findByHoscodeGetHospDetail(@PathVariable String hoscode){
        Map<String,Object> map = hospitalService.findByHoscodeGetHospDetail(hoscode);
        return Result.ok(map);
    }
}
