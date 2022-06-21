package com.tu.yygh.oos;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

public class Test1 {
    public static void main(String[] args) {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5tJzcJJHyYA7HMsF7vCL";
        String accessKeySecret = "CrcLZGYCT4nE5eT3lSBgPFK7MWtnI2";
// 填写Bucket名称，例如examplebucket。
        String bucketName = "yygh-testosstututu";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

// 创建存储空间。
        ossClient.createBucket(bucketName);

// 关闭OSSClient。
        ossClient.shutdown();
    }
}
