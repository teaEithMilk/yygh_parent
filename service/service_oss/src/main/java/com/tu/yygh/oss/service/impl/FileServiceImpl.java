package com.tu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import com.tu.yygh.oss.service.FileService;
import com.tu.yygh.oss.utils.ConstantOssPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    //上传文件到阿里云oss
    @Override
    public String upload(MultipartFile file) {
        try {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = ConstantOssPropertiesUtils.EDNPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRECT;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantOssPropertiesUtils.BUCKET;
        // 填写文件名。文件名包含路径，不包含Bucket名称。例如exampledir/exampleobject.txt。
        String objectName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        objectName = uuid + objectName;

        //按照当前日期创建文件夹
        String timeUrl = new DateTime().toString("yyyy/MM/dd");
        objectName = timeUrl + "/" + objectName;

            // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        PutObjectResult putObjectResult = ossClient.putObject(bucketName, objectName, file.getInputStream());

        //获取路径
        String url = "https://"+bucketName+"."+endpoint+"/"+objectName;

        // 关闭OSSClient。
         ossClient.shutdown();

         return url;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void Test1(){
        List list = new ArrayList();

        List list2 = new ArrayList();

        Object object = new Object();

        object.equals(1);

        list.equals(list);
    }
}
