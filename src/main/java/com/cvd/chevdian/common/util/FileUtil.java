package com.cvd.chevdian.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

@Component
@Slf4j
public class FileUtil {

    private static String accessKeyId;

    private static String accessKeySecret;

    private static String endpoint;

    private static String bucketName;

    @Value("${aliyun.accessKeyId}")
    public void setAccessKeyId(String accessKeyId) {
        FileUtil.accessKeyId = accessKeyId;
    }

    @Value("${aliyun.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        FileUtil.accessKeySecret = accessKeySecret;
    }

    @Value("${aliyun.endpoint}")
    public void setEndpoint(String endpoint) {
        FileUtil.endpoint = endpoint;
    }

    @Value("${aliyun.bucketName}")
    public void setBucketName(String bucketName) {
        FileUtil.bucketName = bucketName;
    }

    /**
     * 根据文件内容返回string
     *
     * @param fileName
     * @return
     */
    public static String getFileJsonToString(String fileName) {
        File file = new File(fileName);
        StringBuffer sb = new StringBuffer();
        BufferedReader bfr = null;
        try {
            bfr = new BufferedReader(new FileReader(file));
            String s;
            while ((s = bfr.readLine()) != null) {
                sb.append(s);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bfr != null) {
                try {
                    bfr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 根据文件内容返回json对象
     *
     * @param fileName
     * @return
     */
    public static JSONObject getFileJsonToJson(String fileName) {
        String sb = getFileJsonToString(fileName);
        return JSON.parseObject(sb);
    }

    /**
     * 根据路径返回properties
     *
     * @param fileName
     * @return
     */
    public static Properties getProperties(String fileName) {
        Properties prop = new Properties();

        InputStream in = FileUtil.class.getResourceAsStream("/" + fileName);
        try {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return prop;
    }

    /**
     * 根据路径和key，获取value
     *
     * @param fileName
     * @param key
     * @return
     */
    public static String getPropertiesValueByKey(String fileName, String key) {
        Properties prop = getProperties(fileName);
        String value = prop.getProperty(key);
        return value;

    }

    public static void main(String[] args) {
        String str = getFileJsonToString("E:\\idea_works\\chevdian\\src\\main\\resources\\weixin\\menu.json");
        System.out.println(str);
    }

    /**
     * OSS上传文件
     */
    public static boolean saveImg(String folderName, String fileName, InputStream in) {
        try {
            // 创建OSSClient实例。
            OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);

            String key = folderName + fileName;
            ossClient.putObject(bucketName, key, in);

            // 关闭OSSClient。
            ossClient.shutdown();
            return true;
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    /**
     * OSS 获取文件url
     */
    public static String getImgURLFromOSS(String folderName, String fileName) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        String key = folderName + fileName;
        // 设置URL过期时间为1小时
        Date expiration = new Date(new Date().getTime() + 3600 * 1000);
        // 生成URL
        URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
        ossClient.shutdown();
        return url.toString();
    }
}
