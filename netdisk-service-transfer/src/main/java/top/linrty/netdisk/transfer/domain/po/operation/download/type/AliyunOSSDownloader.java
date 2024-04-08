package top.linrty.netdisk.transfer.domain.po.operation.download.type;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.download.Downloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.entity.DownloadFile;

import java.io.InputStream;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Component
public class AliyunOSSDownloader extends Downloader {

    private AliyunConfig aliyunConfig;

    private OSS ossClient;

    @Override
    public void prepareDownload() {
        this.ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
    }

    @Override
    public InputStream getInputStream(DownloadFile downloadFile) {

        OSSObject ossObject;
        if (downloadFile.getRange() != null) {
            ossObject = ossClient.getObject(
                    new GetObjectRequest(
                            this.aliyunConfig.getBucketName(),
                            FileTypeUtil.getAliyunObjectNameByFileUrl(downloadFile.getFileUrl())
                    ).withRange(
                            downloadFile.getRange().getStart(),
                            downloadFile.getRange().getStart() + (long)downloadFile.getRange().getLength() - 1L
                    )
            );
        } else {
            ossObject = ossClient.getObject(
                    this.aliyunConfig.getBucketName(),
                    FileTypeUtil.getAliyunObjectNameByFileUrl(downloadFile.getFileUrl())
            );
        }

        InputStream inputStream = ossObject.getObjectContent();
        downloadFile.setOssClient(ossClient);
        return inputStream;
    }

    @Override
    public void doDownloadFinish() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

}
