package com.tu.yygh.user.service.impl;

import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tu.yygh.common.exception.YyghException;
import com.tu.yygh.common.helper.JwtHelper;
import com.tu.yygh.common.result.ResultCodeEnum;
import com.tu.yygh.user.mapper.UserInfoMapper;
import com.tu.yygh.user.service.PatientService;
import com.tu.yygh.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;



    /**
     * 用户手机号登录
     * */
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //从loginVo获取输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //判断手机号和验证码是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            //抛出异常，参数不正确
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //判断手机验证码和输入的验证码是否一致
        String Redis_phone = redisTemplate.opsForValue().get(phone);
        if(!Redis_phone.equals(code)){
            //抛出异常，验证码错误
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //微信登录，绑定手机号码
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())){
            //有了openID表示微信登录，绑定手机号
            //根据openID获取数据
            userInfo = this.selectWxInfoByOpenId(loginVo.getOpenid());
            if(userInfo != null){
                userInfo.setPhone(phone);
                this.baseMapper.updateById(userInfo);
            }
        }

        //userInfo等于空，手机号登录
        if(userInfo == null){
            //判断是否是第一次登录，根据手机号查询数据库，如果不存在相同的手机号就是第一次登录，就可以注册
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<UserInfo>();
            queryWrapper.eq("phone",phone);
            userInfo = baseMapper.selectOne(queryWrapper);
            if(userInfo == null){
                //第一次使用这个手机号登录，进行注册
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                this.save(userInfo);
            }
        }
        //校验是否被禁用
        if(userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //不是第一次进行登录
        //返回登录信息
        //返回登录用户名
        //返回token
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);

        //token生成
        String token = JwtHelper.createToken(userInfo.getId(), name);

        map.put("token", token);
        return map;
    }

    /**
     * 根据openID查询数据库微信用户
     **/
    @Override
    public UserInfo selectWxInfoByOpenId(String openId) {
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(UserInfo::getOpenid,openId);

        UserInfo userInfo = baseMapper.selectOne(queryWrapper);

        return userInfo;
    }

    /**
     *   用户认证接口
     * */
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户ID查询用户ID
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //姓名
        userInfo.setName(userAuthVo.getName());
        //证件类型
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        //证件编号
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        //证件照片
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        //证件状态，或是认证中，或是认证失败，成功等
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    /**
     *  获取用户ID信息接口
     * */
    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = baseMapper.selectById(userId);

        return userInfo;
    }

    //用户列表条件带分页
    //用户列表（条件查询带分页）
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {

        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("name",name);
        }
        if(!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }


        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, wrapper);

        userInfoPage.getRecords().stream().forEach(item -> {
            this.packageUserInfo(item);
        });

        return userInfoPage;
    }

    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }

    /**
     * 用户锁定
     * @param userId
     * @param status 0：锁定 1：正常
     */

    @Override
    public void lock(Long userId, Integer status) {
        if(status.intValue() == 0 || status.intValue() == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }


    /**
     * 详情
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String,Object> map = new HashMap<>();
        //根据userid查询用户信息
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
        map.put("userInfo",userInfo);
        //根据userid查询就诊人信息
        List<Patient> patientList = patientService.findAllUserId(userId);
        map.put("patientList",patientList);
        return map;
    }

    /**
     * 认证审批
     * @param userId
     * @param authStatus 2：通过 -1：不通过
     */
    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus.intValue()==2 || authStatus.intValue()==-1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

}
