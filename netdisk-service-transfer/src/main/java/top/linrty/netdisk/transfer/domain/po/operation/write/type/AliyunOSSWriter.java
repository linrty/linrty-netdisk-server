package top.linrty.netdisk.transfer.domain.po.operation.write.type;

import com.aliyun.oss.OSS;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.write.Writer;
import top.linrty.netdisk.transfer.domain.po.operation.write.entity.WriteFile;

import java.io.InputStream;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class AliyunOSSWriter extends Writer {

    private AliyunConfig aliyunConfig;

    public void write(InputStream inputStream, WriteFile writeFile) {
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
        ossClient.putObject(this.aliyunConfig.getBucketName(), FileTypeUtil.getAliyunObjectNameByFileUrl(writeFile.getFileUrl()), inputStream);
        ossClient.shutdown();
    }
}
