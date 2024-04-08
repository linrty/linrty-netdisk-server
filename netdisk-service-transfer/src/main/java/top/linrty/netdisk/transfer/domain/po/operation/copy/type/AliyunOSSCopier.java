package top.linrty.netdisk.transfer.domain.po.operation.copy.type;

import com.aliyun.oss.OSS;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.copy.Copier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.entity.CopyFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Slf4j
public class AliyunOSSCopier extends Copier {
    private AliyunConfig aliyunConfig;

    public String copy(InputStream inputStream, CopyFile copyFile) {
        String uuid = UUID.randomUUID().toString();
        String fileUrl = FileTypeUtil.getUploadFileUrl(uuid, copyFile.getExtendName());
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);

        try {
            ossClient.putObject(this.aliyunConfig.getBucketName(), fileUrl, inputStream);
        }finally {
            IOUtils.closeQuietly(inputStream);
            ossClient.shutdown();
        }

        return fileUrl;
    }
}