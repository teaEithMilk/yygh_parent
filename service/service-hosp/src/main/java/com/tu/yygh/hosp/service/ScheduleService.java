package com.tu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.awt.*;
import java.util.List;
import java.util.Map;

public interface ScheduleService {
    /**
     * 上传排班信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    /**
     * 分页查询
     * @param page 当前页码
     * @param limit 每页记录数
     * @param scheduleQueryVo 查询条件
     * @return
     */
    Page<Schedule> selectPage(Integer page, Integer limit, ScheduleQueryVo scheduleQueryVo);

    /**
     * 删除科室
     * @param hoscode
     * @param hosScheduleId
     */
    void remove(String hoscode, String hosScheduleId);

    /**
     * 根据医院编号 和 科室编号 ，查询排班规则数据
     * */
    Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode);


    /**
     * 根据医院编号 和 科室编号 和 排班日期查询排班详细信息
     * */
    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

}
