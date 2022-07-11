package com.tu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tu.yygh.common.exception.YyghException;
import com.tu.yygh.common.result.ResultCodeEnum;
import com.tu.yygh.hosp.repository.ScheduleRepository;
import com.tu.yygh.hosp.service.DepartmentService;
import com.tu.yygh.hosp.service.HospitalService;
import com.tu.yygh.hosp.service.ScheduleService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    //获取可预约的排班数据
    @Override
    public Map<String,Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String,Object> result = new HashMap<>();
        //根据医院编号获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(hospital == null){
            throw  new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约日期的数据，分页
        IPage iPage = this.getListDate(page, limit, bookingRule);
        //当前可预约日期
        List<Date> dateList = iPage.getRecords();

        //获取可预约日期科室剩余预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();
        //获取科室剩余预约数

        //合并数据 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream().collect(
                    Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for(int i=0, len=dateList.size(); i<len; i++) {
            Date date = dateList.get(i);

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if(null == bookingScheduleRuleVo) { // 说明当天没有排班医生
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == len-1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        Hospital hospital2 =  hospitalService.getByHoscode(hoscode);
        baseMap.put("hosname", hospital2.getHosname());
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
//月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
//放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
//停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    //获取可预约日期的分页数据
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        int cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if(releaseTime.isBeforeNow()) cycle += 1;
        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
        //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        if(end >dateList.size()) end = dateList.size();
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, 7, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }


    /**
     * 根据id获取排班
     * @param id
     * @return
     */
    @Override
    public Schedule getById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        //封装数据
        return this.packSchedule(schedule);
    }


    //封装数据
    private Schedule packSchedule(Schedule schedule) {
        //获取医院编号
        String hoscode = schedule.getHoscode();
        //获取科室编号
        String depcode = schedule.getDepcode();

        //医院名称
        String hosname = hospitalService.getByHoscode(hoscode).getHosname();

        //科室名称
        String depname = departmentService.getByDepCode(depcode).getDepname();

        Map<String,Object> map = new HashMap<>();

        map.put("hosname",hosname);
        map.put("depname",depname);

        schedule.setParam(map);

        return schedule;
    }

    //根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //排班信息
        Schedule schedule = this.getById(scheduleId);
        if(null == schedule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        return scheduleOrderVo;
    }


}
