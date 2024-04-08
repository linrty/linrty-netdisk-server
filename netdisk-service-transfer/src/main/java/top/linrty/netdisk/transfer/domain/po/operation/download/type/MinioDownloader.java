package top.linrty.netdisk.transfer.domain.po.operation.download.type;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.transfer.domain.po.operation.download.Downloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.entity.DownloadFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Getter
@Setter
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MinioDownloader extends Downloader {

    private MinioConfig minioConfig;

    private MinioClient minioClient;

    @Override
    public void prepareDownload() {
        this.minioClient = MinioClient.builder()
                .endpoint(this.minioConfig.getEndpoint())
                .credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey())
                .build();
    }

    @Override
    public InputStream getInputStream(DownloadFile downloadFile) {
        InputStream inputStream = null;
        try {
            if (downloadFile.getRange() != null) {
                inputStream = this.minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(this.minioConfig.getBucketName())
                                .object(downloadFile.getFileUrl())
                                .offset(downloadFile.getRange().getStart())
                                .length((long)downloadFile.getRange().getLength())
                                .build()
                );
            } else {
                inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(this.minioConfig.getBucketName())
                                .object(downloadFile.getFileUrl())
                                .build()
                );
            }
        } catch (MinioException e) {
            log.error("Error occurred: " + e);
            throw new FileOperationException("Minio下载文件失败", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            log.error(e.getMessage());
            throw new FileOperationException("Minio下载文件失败", e);
        }
        return inputStream;
    }

    @Override
    public void doDownloadFinish() {

    }
}
