package com.tu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tu.yygh.cmn.listener.DictListener;
import com.tu.yygh.cmn.mapper.DictMapper;
import com.tu.yygh.cmn.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //导入数据字典
    @CacheEvict(value = "dict",allEntries = true)
    @Override
    public void importData(MultipartFile file) {
        try {
            EasyExcel.read(
                    file.getInputStream(),
                    DictEeVo.class,
                    new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //导出数据字典
    @Override
    public void exportDictData(HttpServletResponse response) {
        try {
            //设置下载信息
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = "dict";
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            //查询数据库
            List<Dict> dictList = baseMapper.selectList(null);

            List<DictEeVo> dictVoList = new ArrayList<>();

            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictVo);
                dictVoList.add(dictVo);
            }

            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据数据Id查询子数据列表
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
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

    /**
     * 根据DictCode和value值获取数据字典名称
     * */
    @Override
    public String getName(String parentDictCode, String value) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();

        if(parentDictCode == null || parentDictCode == ""){
            //根据value值查询
            queryWrapper.eq(Dict::getValue,value);
            Dict dict = baseMapper.selectOne(queryWrapper);
            return dict.getName();
        }else{//parentDictCode不为空，根据DictCode和value值获取
            //根据dictCod获取dict对象，获得dict的id值
            queryWrapper.eq(Dict::getDictCode,parentDictCode);
            Dict codeDict = baseMapper.selectOne(queryWrapper);
            Long id = codeDict.getId();
            //根据id和value值查询
            Dict dict = baseMapper.selectOne(
                    new LambdaQueryWrapper<Dict>()
                            .eq(Dict::getParentId, id)
                            .eq(Dict::getValue, value));
            return dict.getName();
        }
    }

    @Override
    public List<Dict> findbyDictCode(String dictCode) {
        //根据dictCode查询到ID
        Dict dict = baseMapper.selectOne(new LambdaQueryWrapper<Dict>().eq(Dict::getDictCode,dictCode));

        //根据ID查询子字典
        List<Dict> dicts = baseMapper.selectList(
                new LambdaQueryWrapper<Dict>()
                        .eq(Dict::getParentId, dict.getId()));
        return dicts;
    }
}
