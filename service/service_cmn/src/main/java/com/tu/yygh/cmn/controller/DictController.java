package com.tu.yygh.cmn.controller;

import com.atguigu.yygh.model.cmn.Dict;
import com.tu.yygh.cmn.service.DictService;
import com.tu.yygh.common.result.Result;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/admin/cmn/dict")
@Api(tags = "Dict设置管理")
//跨域注解
@CrossOrigin
public class DictController{

    @Autowired
    private DictService dictService;

   //根据数据Id查询子数据列表
    @GetMapping("/findChlidData{id}")
    public Result findChlidData(Long id){
        List<Dict> list =  dictService.findChlidData(id);
        return Result.ok(list);
    }

}
