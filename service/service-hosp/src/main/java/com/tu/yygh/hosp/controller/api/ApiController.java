package com.tu.yygh.hosp.controller.api;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.tu.yygh.common.exception.YyghException;
import com.tu.yygh.common.helper.HttpRequestHelper;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.common.result.ResultCodeEnum;
import com.tu.yygh.common.utils.MD5;
import com.tu.yygh.hosp.service.DepartmentService;
import com.tu.yygh.hosp.service.HospitalService;
import com.tu.yygh.hosp.service.HospitalSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //获取传递过来的医院信息，转为Object类型
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //必须参数校验 略
        String hoscode = (String)paramMap.get("hoscode");
        //必填
        String depcode = (String)paramMap.get("depcode");

        isSign(paramMap);

        departmentService.remove(hoscode, depcode);

        return Result.ok();
    }

    /**
     * 查询科室接口
     * */
    @ApiOperation(value = "查询科室")
    @PostMapping("/department/list")
    public Result findDepartment(HttpServletRequest request){
        //获取传递过来的医院信息，转为Object类型
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //获取页码
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        //页大小
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));

        //验证签名
        isSign(paramMap);

        //查询条件
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(paramMap.get("hoscode").toString());

        //调用方法
        Page<Department> DepartmentList =  departmentService.findPageDepartment(page,limit,departmentQueryVo);

        return Result.ok(DepartmentList);
    }
    /**
     * 上传科室接口
     * */
    @ApiOperation(value = "上传科室")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //获取传递过来的医院信息，转为Object类型
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //验证签名
        isSign(paramMap);

        departmentService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "查询医院")
    @PostMapping("/hospital/show")
    public Result getHospital(HttpServletRequest request){
        //获取传递过来的医院信息，转为Object类型
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        isSign(paramMap);

        //查询医院
        Hospital hospital = hospitalService.getByHoscode(paramMap.get("hoscode").toString());

        return Result.ok(hospital);
    }

    @ApiOperation(value = "上传医院")
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request){
        //获取传递过来的医院信息，转为Object类型
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //验证签名方法
        isSign(paramMap);

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String) paramMap.get("logoData");
        if(!logoDataString.isEmpty()){
            String logoData = logoDataString.replaceAll(" ", "+");
            paramMap.put("logoData",logoData);
        }

        //调用service保存方法
        hospitalService.save(paramMap);
        return Result.ok();
    }

    //验证签名
    private void isSign(Map<String, Object> paramMap){
        //1.获取医院系统传递的签名，签名进行MD5加密
        String sign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编码，查询数据库，查询签名
        String hoscode = (String) paramMap.get("hoscode");
        String SignKey = hospitalSetService.getSignKey(hoscode);

        //把数据库查询出来的签名MD5加密
        String SignKeyMD5 = MD5.encrypt(SignKey);

        //判断签名是否一致,不一致，抛出异常
        if(!sign.equals(SignKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }

}
