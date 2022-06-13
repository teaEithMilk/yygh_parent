package com.tu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.tu.yygh.hosp.repository.DepartmentRepository;
import com.tu.yygh.hosp.service.DepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     *
     *  上传科室接口
     *
     * */
    @Override
    public void save(Map<String, Object> paramMap) {
        //把参数map集合转为实体类 Hospital,先转为json类型
        String mapString = JSONObject.toJSONString(paramMap);
        //传入实体类class，转为对象
        Department department = JSONObject.parseObject(mapString, Department.class);

        //判断是否存在数据,在hospitalRepository中继承了MongoRepository，只要遵守方法名规则，不需要实现方法
        Department departmentExits  = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        //修改，需要传递ID值
        if(departmentExits != null){
            departmentExits.setUpdateTime(new Date());
            departmentExits.setIsDeleted(0);
            departmentRepository.save(departmentExits);
        }else{
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    /**
     * 分页查询
     * @param page 当前页码
     * @param limit 每页记录数
     * @param departmentQueryVo 查询条件
     * @return
     */
    @Override
    public Page<Department> findPageDepartment(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页
        Pageable pageable = PageRequest.of(page-1, limit, sort);

        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

//创建实例
        Example<Department> example = Example.of(department, matcher);
        return departmentRepository.findAll(example, pageable);
    }

    /**
     * 删除科室
     * @param hoscode
     * @param depcode
     */
    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        if(department != null){
            departmentRepository.deleteById(department.getId());
        }
    }

    /**
     * 根据医院编号，查询医院所有科室列表
     * **/
    @Override
    public List<DepartmentVo> findDeptTree(String hosCode) {
        //最终返回数据
        List<DepartmentVo> result = new ArrayList<>();


        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hosCode);
        //条件
        Example<Department> of = Example.of(departmentQuery);

        List<Department> departmentList = departmentRepository.findAll(of);

        //根据大科室编号，bigCode分组，获取每个大科室里面的小科室
        Map<String, List<Department>> departmentMap =
                departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));

        //分组后，遍历集合
        for(Map.Entry<String,List<Department>> entry : departmentMap.entrySet()){
            //获取大科室编号
            String key = entry.getKey();

            //大科室编号对应的数据
            List<Department> value = entry.getValue();

            //封装大科室
            DepartmentVo departmentBig = new DepartmentVo();
            departmentBig.setDepcode(key);
            departmentBig.setDepname(value.get(0).getBigname());

            //封装小科室
            ArrayList<DepartmentVo> children = new ArrayList<>();
            for (Department child : value) {
                DepartmentVo departmentSmall = new DepartmentVo();
                departmentSmall.setDepcode(child.getDepcode());
                departmentSmall.setDepname(child.getDepname());
                children.add(departmentSmall);
            }

            //将小科室封装到大科室
            departmentBig.setChildren(children);

            //放入最终的result中
            result.add(departmentBig);
        }

        return result;
    }
}
