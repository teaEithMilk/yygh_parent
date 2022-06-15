package com.tu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.tu.yygh.hosp.repository.ScheduleRepository;
import com.tu.yygh.hosp.service.DepartmentService;
import com.tu.yygh.hosp.service.HospitalService;
import com.tu.yygh.hosp.service.ScheduleService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    /**
     * 上传排班信息
     * @param paramMap
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //把参数map集合转为实体类 Hospital,先转为json类型
        String mapString = JSONObject.toJSONString(paramMap);

        //传入实体类class，转为对象
        Schedule schedule = JSONObject.parseObject(mapString, Schedule.class);

        //判断是否存在数据,在departmentRepository中继承了MongoRepository，只要遵守方法名规则，不需要实现方法
        Schedule scheduleExits  = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());

        //修改
        if(scheduleExits != null){
            scheduleExits.setUpdateTime(new Date());
            scheduleExits.setIsDeleted(0);
            scheduleExits.setStatus(1);
            scheduleRepository.save(scheduleExits);
        }else{
            //增加
            schedule.setUpdateTime(new Date());
            schedule.setCreateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
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
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页
        Pageable pageable = PageRequest.of(page-1, limit, sort);

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Schedule> example = Example.of(schedule, matcher);
        return scheduleRepository.findAll(example, pageable);
    }

    /**
     * 删除科室
     * @param hoscode
     * @param hosScheduleId
     */
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        if(schedule != null){
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    /**
     * 根据医院编号 和 科室编号 ，查询排班规则数据
     * */
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //1、根据医院编号和科室编号查询排班信息
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //2、根据工作日期workDate日期进行分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配条件
                Aggregation.group("workDate")//分组字段
                .first("workDate").as("workDate")
                        //统计号源数量
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //实现分页
                Aggregation.skip((page - 1 ) * limit),
                Aggregation.limit(limit)
        );

        //执行方法,根据上面编写的条件查询到集合，
        // 参数：agg：条件对象，Schedule.class查询实体对象，BookingScheduleRuleVo.class：返回类型的实体对象
        AggregationResults<BookingScheduleRuleVo> aggregate =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();

        //得到总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResult =
                mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        //总记录数
        int total = totalAggResult.getMappedResults().size();

        //根据日期获取到星期几
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            //调用方法，根据日期获取到星期几
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            //封装到集合中的对象中
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //最终返回map集合
        Map<String,Object> result = new HashMap<>();
        //查询的集合
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        //总记录数
        result.put("total",total);

        //其他基础数据
        //获取医院名称
        HashMap<String, Object> baseMap = new HashMap<>();
        baseMap.put("hosname",hospitalService.getByHoscode(hoscode).getHosname());

        result.put("baseMap",baseMap);
        return result;
    }

    /**
     * 根据医院编号 和 科室编号 和 排班日期查询排班详细信息
     * */
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        //1、根据医院编号和科室编号和排班日期查询排班详细信息
        List<Schedule> scheduleList =
                scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());

        //把得到的集合遍历，封装其他数据，医院名称，科室名称，日期对应的星期
        for (Schedule schedule : scheduleList) {
            //调用方法封装其他数据
            this.packageScedule(schedule);
        }
        return scheduleList;
    }

    /**
     * 封装排班详细信息数据
     * */
    private void packageScedule(Schedule schedule) {
        //封装医院名称
        schedule.getParam().put("hosname",hospitalService.getByHoscode(schedule.getHoscode()).getHosname());

        //封装科室名称
        schedule.getParam().put("depname",departmentService.getByDepCode(schedule.getDepcode()).getDepname());

        //封装日期对应的星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
