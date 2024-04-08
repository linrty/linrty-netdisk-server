package top.linrty.netdisk.transfer.domain.po.operation.preview.type;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.config.ThumbImageConfig;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.preview.Previewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.entity.PreviewFile;

import java.io.InputStream;


@Slf4j
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Component
@Builder
public class AliyunOSSPreviewer extends Previewer {
    private AliyunConfig aliyunConfig;

    public AliyunOSSPreviewer(AliyunConfig aliyunConfig, ThumbImageConfig thumbImageConfig) {
        this.aliyunConfig = aliyunConfig;
        this.setThumbImageConfig(thumbImageConfig);
    }

    protected InputStream getInputStream(PreviewFile previewFile) {
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
        OSSObject ossObject = ossClient.getObject(this.aliyunConfig.getBucketName(), FileTypeUtil.getAliyunObjectNameByFileUrl(previewFile.getFileUrl()));
        InputStream inputStream = ossObject.getObjectContent();
        previewFile.setOssClient(ossClient);
        return inputStream;
    }

    public AliyunConfig getAliyunConfig() {
        return this.aliyunConfig;
    }

    public void setAliyunConfig(final AliyunConfig aliyunConfig) {
        this.aliyunConfig = aliyunConfig;
    }
}