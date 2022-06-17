package com.tu.yygh.user.service.impl;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tu.yygh.common.exception.YyghException;
import com.tu.yygh.common.helper.JwtHelper;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.common.result.ResultCodeEnum;
import com.tu.yygh.user.mapper.UserInfoMapper;
import com.tu.yygh.user.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

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
}
