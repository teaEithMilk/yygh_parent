package com.tu.yygh.sms.service;

import org.springframework.util.StringUtils;

public interface SmsService {
    /**
     * 发送手机验证码
     * */
    boolean sendPhoneCode(String phone, String Code);
}
