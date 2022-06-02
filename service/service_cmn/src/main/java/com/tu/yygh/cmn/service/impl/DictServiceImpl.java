package com.tu.yygh.cmn.service.impl;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tu.yygh.cmn.mapper.DictMapper;
import com.tu.yygh.cmn.service.DictService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //根据数据Id查询子数据列表
    @Override
    public List<Dict> findChlidData(Long id) {

        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Dict::getParentId,id);

        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        //向List集合中循环遍历添加是否有子节点属性：true,false
        for (Dict dict : dicts) {
            dict.setHasChildren(this.isChildren(dict.getId()));
        }
        return dicts;
    }

    //判断Id是否有子节点
    private boolean isChildren(Long id) {

        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Dict::getParentId,id);

        if(baseMapper.selectList(queryWrapper) != null) {
            return true;
        }
        return false;
    }


}
