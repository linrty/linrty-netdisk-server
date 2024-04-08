package top.linrty.netdisk.common.config;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QiniuyunConfig {

    private String domain;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

}
