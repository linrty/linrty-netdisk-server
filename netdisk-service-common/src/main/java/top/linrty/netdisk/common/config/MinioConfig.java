package top.linrty.netdisk.common.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MinioConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;
}
