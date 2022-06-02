package com.tu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DictService extends IService<Dict> {

    //根据数据Id查询子数据列表
    List<Dict> findChlidData(Long id);
}
