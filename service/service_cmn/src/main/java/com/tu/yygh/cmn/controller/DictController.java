package com.tu.yygh.cmn.controller;

import com.atguigu.yygh.model.cmn.Dict;
import com.tu.yygh.cmn.service.DictService;
import com.tu.yygh.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/admin/cmn/dict")
@Api(tags = "Dict设置管理")
public class DictController{

    @Autowired
    private DictService dictService;

    /**
     * 根据dictCode获取下级节点
     * */
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result findbyDictCode(@PathVariable String dictCode){
        List<Dict> dicts =  dictService.findbyDictCode(dictCode);
        return Result.ok(dicts);
    }

    //导出数据字典
    @ApiOperation(value="导出")
    @GetMapping("/exportData")
    public void exportData(HttpServletResponse response){
        dictService.exportDictData(response);
    }

    /**
     * 导入数据字典，用MultipartFile获得上传文件
     * */
    @ApiOperation(value = "导入")
    @PostMapping("importData")
    public Result importData(MultipartFile file) {
        dictService.importData(file);
        return Result.ok();
    }


    @ApiOperation("根据数据Id查询子数据列表")
   //根据数据Id查询子数据列表
    @GetMapping("/findChlidData/{id}")
    public Result findChlidData(@PathVariable Long id){
        List<Dict> list =  dictService.findChlidData(id);
        return Result.ok(list);
    }

    /**
     *  根据DictCode和value值获取数据字典名称
     * */
    @ApiOperation(value = "根据DictCode和value值获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @ApiParam(name = "parentDictCode", value = "上级编码", required = true)
            @PathVariable("parentDictCode") String parentDictCode,

            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getName(parentDictCode, value);

    }

    /**
     *  根据value值获取数据字典名称
     * */
    @ApiOperation(value = "根据value值获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getName("",value);
    }


}
