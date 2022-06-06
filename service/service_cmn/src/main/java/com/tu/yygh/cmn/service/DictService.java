package com.tu.yygh.cmn.service;

import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {

    //根据数据Id查询子数据列表
    List<Dict> findChlidData(Long id);

    //导出数据字典
    void exportDictData(HttpServletResponse response);

    void importData(MultipartFile file);
}
