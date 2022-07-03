package com.tu.yygh.user.api;

import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.common.util.AuthContextHolder;
import com.tu.yygh.user.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@Api(tags = "用户信息接口")
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 用户手机号登录
     * */
    @ApiOperation(value = "会员登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo, HttpServletRequest request) {
//        loginVo.setIp(IpUtil.getIpAddr(request));
        Map<String, Object> info = userInfoService.login(loginVo);
        return Result.ok(info);
    }

    /**
    *   用户认证接口
    * */
    @PostMapping("/auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo,HttpServletRequest request){
        //传用户ID和数据Vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }

    /**
     *  获取用户ID信息接口
     * */
    @GetMapping("/auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        //根据传过来的token获取用户ID
        Long userId = AuthContextHolder.getUserId(request);

        UserInfo userInfo =  userInfoService.getUserInfo(userId);

        return Result.ok(userInfo);
    }



}
