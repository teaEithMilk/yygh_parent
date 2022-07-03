package com.tu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    /**
     * 用户手机号登录
     * */
    Map<String, Object> login(LoginVo loginVo);

    /**
     * 根据openID查询数据库微信用户
     **/
    UserInfo selectWxInfoByOpenId(String openId);

    /**
     *   用户认证接口
     * */
    void userAuth(Long userId, UserAuthVo userAuthVo);


    /**
     *  获取用户ID信息接口
     * */
    UserInfo getUserInfo(Long userId);

    //用户列表条件带分页
    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    /**
     * 用户锁定
     * @param userId
     * @param status 0：锁定 1：正常
     */
    void lock(Long userId, Integer status);


    /**
     * 详情
     * @param userId
     * @return
     */
    Map<String, Object> show(Long userId);

    /**
     * 认证审批
     * @param userId
     * @param authStatus 2：通过 -1：不通过
     */
    void approval(Long userId, Integer authStatus);



}
