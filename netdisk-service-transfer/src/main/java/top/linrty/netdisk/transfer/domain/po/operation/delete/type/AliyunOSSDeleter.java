package top.linrty.netdisk.transfer.domain.po.operation.delete.type;

import com.aliyun.oss.OSS;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.delete.Deleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.entity.DeleteFile;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Slf4j
@Component
public class AliyunOSSDeleter extends Deleter {

    private AliyunConfig aliyunConfig;


    public void delete(DeleteFile deleteFile) {
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);

        try {
            ossClient.deleteObject(this.aliyunConfig.getBucketName(), FileTypeUtil.getAliyunObjectNameByFileUrl(deleteFile.getFileUrl()));
        } finally {
            ossClient.shutdown();
        }

        this.deleteCacheFile(deleteFile);
    }
}
