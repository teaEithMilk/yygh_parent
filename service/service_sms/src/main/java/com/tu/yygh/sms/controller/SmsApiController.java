package com.tu.yygh.sms.controller;

import com.tu.yygh.common.result.Result;
import com.tu.yygh.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sms")
public class SmsApiController {

    @Autowired
    private SmsService smsService;
    //redis
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //发送手机验证码
    @GetMapping("/sendPhoneCode/{Phone}")
    public Result sendPhoneCode(@PathVariable String Phone){
        //从redis获取验证码，如果获取到，返回ok
        String phoneCode = redisTemplate.opsForValue().get("phoneCode");
        if(!StringUtils.isEmpty(phoneCode)){
            return Result.ok();
        }
        String Code = (int)((Math.random()*9+1)*100000)+"";
        //如果从redis获取不到，生成验证码发送
        boolean isSend = smsService.sendPhoneCode(Phone,Code);
        if(isSend){
            redisTemplate.opsForValue().set(Phone,Code,5, TimeUnit.MINUTES);
            return Result.ok();
        }
        return Result.fail().message("验证码发送失败");
    }
}
