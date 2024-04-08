package top.linrty.netdisk.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import top.linrty.netdisk.common.config.AliyunConfig;

public class AliyunUtil {
    public static OSS getOSSClient(AliyunConfig aliyunConfig) {
        OSS ossClient = (new OSSClientBuilder()).build(aliyunConfig.getEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        return ossClient;
    }
}
