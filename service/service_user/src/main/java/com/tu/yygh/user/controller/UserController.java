package com.tu.yygh.user.controller;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tu.yygh.common.result.Result;
import com.tu.yygh.user.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@Api(tags = "用户管理接口")
public class UserController {

    @Autowired
    public UserInfoService userInfoService;

    //用户列表条件带分页
    //用户列表（条件查询带分页）
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pageParam = new Page<>(page,limit);
        IPage<UserInfo> pageModel =
                userInfoService.selectPage(pageParam,userInfoQueryVo);
        return Result.ok(pageModel);
    }

    /**
     * 用户锁定
     * @param userId
     * @param status 0：锁定 1：正常
     */

    @ApiOperation(value = "锁定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(
            @PathVariable("userId") Long userId,
            @PathVariable("status") Integer status){
        userInfoService.lock(userId, status);
        return Result.ok();
    }

    /**
     * 详情
     * @param userId
     * @return
     */
    //用户详情
    @GetMapping("show/{userId}")
    public Result show(@PathVariable String userId) {
        Long id = Long.parseLong(userId);
        Map<String,Object> map = userInfoService.show(id);
        return Result.ok(map);
    }

    //认证审批
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return Result.ok();
    }

}
