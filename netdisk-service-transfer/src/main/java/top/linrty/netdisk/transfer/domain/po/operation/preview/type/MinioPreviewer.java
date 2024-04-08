package top.linrty.netdisk.transfer.domain.po.operation.preview.type;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.config.ThumbImageConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.transfer.domain.po.operation.preview.Previewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.entity.PreviewFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@Builder
public class MinioPreviewer extends Previewer {
    private MinioConfig minioConfig;
    public MinioPreviewer(MinioConfig minioConfig, ThumbImageConfig thumbImageConfig) {
        this.setMinioConfig(minioConfig);
        this.setThumbImageConfig(thumbImageConfig);
    }

    protected InputStream getInputStream(PreviewFile previewFile) {
        InputStream inputStream = null;

        try {
            MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
            inputStream = minioClient.getObject((GetObjectArgs)((GetObjectArgs.Builder)((GetObjectArgs.Builder)GetObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(previewFile.getFileUrl())).build());
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            throw new FileOperationException("MiniO下载文件失败", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            log.error(e.getMessage());
            throw new FileOperationException("MiniO下载文件失败", e);
        }

        return inputStream;
    }
}