package com.tu.yygh.sms.service.impl;

import com.cloopen.rest.sdk.BodyType;
import com.cloopen.rest.sdk.CCPRestSmsSDK;
import com.tu.yygh.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;

@Service
public class SmsServiceImpl implements SmsService {

    //redis
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发送手机验证码
     * */
    @Override
    public boolean sendPhoneCode(String phone,String Code) {
        //生产环境请求地址：app.cloopen.com
        String serverIp="app.cloopen.com";
        //请求端口
        String serverPort="8883";
        //主账号,登陆云通讯网站后,可在控制台首页看到开发者主账号ACCOUNT SID和主账号令牌AUTH TOKEN
        String accountSId="8a216da880d67afb01816a7caf671bab";
        String accountToken="1dca90925198495bbfe4b507e83af828";
        //请使用管理控制台中已创建应用的APPID
        String appId="8a216da880d67afb01816a7cb0561bb2";
        CCPRestSmsSDK sdk=new CCPRestSmsSDK();
        sdk.init(serverIp,serverPort);
        sdk.setAccount(accountSId,accountToken);
        sdk.setAppId(appId);
        sdk.setBodyType(BodyType.Type_JSON);
        //发送短信至手机号
        String to=phone;
        //短信模板
        String templateId="1";
        //控制台输出验证码
        System.out.println("手机短信验证码--------------->"+Code);
        //验证码为生成的随机数，5分钟内到期
        String[] datas={Code,"5"};
        HashMap<String,Object> result = sdk.sendTemplateSMS(to,templateId,datas);
        if("000000".equals(result.get("statusCode"))){
            //正常返回输出data包体信息（map）
            HashMap<String,Object> data = (HashMap<String,Object>) result.get("data");
            Set<String> keySet = data.keySet();
            for(String key:keySet){
                Object object = data.get(key);
                System.out.println(key +" = "+object);
            }
            return true;
        }else{
            //异常返回输出错误码和错误信息
            System.out.println("错误码=" + result.get("statusCode") +" 错误信息= "+result.get("statusMsg"));
        }
        return false;
    }
}
