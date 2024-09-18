package com.qiaopi.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.qiaopi.constant.MessageConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;


    /**
     * 文件上传
     * @param file
     * @return
     */
    public String upload(MultipartFile file) {
        String url = "";
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {

            // 获取上传的文件的输入流
            InputStream inputStream = file.getInputStream();

            // 避免文件覆盖
            String originalFilename = file.getOriginalFilename();
            String extname = originalFilename.substring(originalFilename.lastIndexOf("."));
            //判断文件格式阉割版
            if(extname == null || "".equals(extname)|| (!".jpg".equals(extname)&& !".png".equals(extname) && !".jpeg".equals(extname))){
                throw new RuntimeException(MessageConstant.UPLOAD_TYPE_ERROR);
            }
            String fileName = UUID.randomUUID().toString() + extname;

            //文件访问路径
            url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;

            //上传文件到 OSS
            ossClient.putObject(bucketName, fileName, inputStream);

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch (IOException e) {
            System.out.println("Caught an IOException.");
            System.out.println("Error Message:" + e.getMessage());
        }
        catch (RuntimeException re) {
            System.out.println("Caught an RuntimeException.");
            System.out.println("Error Message:" +re.getMessage());
            throw new RuntimeException(re.getMessage());
        }
        finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return url;// 把上传到oss的路径返回
    }
}
