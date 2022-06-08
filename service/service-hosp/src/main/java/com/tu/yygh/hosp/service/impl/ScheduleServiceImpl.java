package com.tu.yygh.hosp.service.impl;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.tu.yygh.hosp.service.ScheduleService;
import org.springframework.data.domain.Page;

import java.util.Map;

public class ScheduleServiceImpl implements ScheduleService {
    /**
     * 上传排班信息
     * @param paramMap
     */
    @Override
    public void save(Map<String, Object> paramMap) {

    }
    /**
     * 分页查询
     * @param page 当前页码
     * @param limit 每页记录数
     * @param scheduleQueryVo 查询条件
     * @return
     */
    @Override
    public Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo) {
        return null;
    }

    /**
     * 删除科室
     * @param hoscode
     * @param hosScheduleId
     */
    @Override
    public void remove(String hoscode, String hosScheduleId) {

    }
}
