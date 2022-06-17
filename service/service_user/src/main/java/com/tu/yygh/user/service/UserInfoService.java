package com.tu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
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

}
