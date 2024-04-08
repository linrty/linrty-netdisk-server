package top.linrty.netdisk.transfer.domain.po.operation.read.type;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.common.util.ReadFileUtil;
import top.linrty.netdisk.transfer.domain.po.operation.read.Reader;
import top.linrty.netdisk.transfer.domain.po.operation.read.entity.ReadFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class AliyunOSSReader extends Reader {
    private AliyunConfig aliyunConfig;

    public String read(ReadFile readFile) {
        String fileUrl = readFile.getFileUrl();
        String fileType = FilenameUtils.getExtension(fileUrl);
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
        OSSObject ossObject = ossClient.getObject(this.aliyunConfig.getBucketName(), FileTypeUtil.getAliyunObjectNameByFileUrl(fileUrl));
        InputStream inputStream = ossObject.getObjectContent();

        String content;
        try {
            content = ReadFileUtil.getContentByInputStream(fileType, inputStream);
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败", e);
        } finally {
            ossClient.shutdown();
        }

        return content;
    }

    public InputStream getInputStream(String fileUrl) {
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
        OSSObject ossObject = ossClient.getObject(this.aliyunConfig.getBucketName(), FileTypeUtil.getAliyunObjectNameByFileUrl(fileUrl));
        return ossObject.getObjectContent();
    }
}
