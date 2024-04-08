package top.linrty.netdisk.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AliyunConfig {
    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;

    private String objectName;
}