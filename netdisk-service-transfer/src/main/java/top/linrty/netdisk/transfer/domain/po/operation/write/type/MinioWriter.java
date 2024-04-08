package top.linrty.netdisk.transfer.domain.po.operation.write.type;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.write.Writer;
import top.linrty.netdisk.transfer.domain.po.operation.write.entity.WriteFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class MinioWriter extends Writer {
    private MinioConfig minioConfig;

    public void write(InputStream inputStream, WriteFile writeFile) {
        try {
            MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
            boolean isExist = minioClient.bucketExists((BucketExistsArgs)((BucketExistsArgs.Builder)BucketExistsArgs.builder().bucket(this.minioConfig.getBucketName())).build());
            if (!isExist) {
                minioClient.makeBucket((MakeBucketArgs)((MakeBucketArgs.Builder)MakeBucketArgs.builder().bucket(this.minioConfig.getBucketName())).build());
            }

            minioClient.putObject((PutObjectArgs)((PutObjectArgs.Builder)((PutObjectArgs.Builder)PutObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(FileTypeUtil.getAliyunObjectNameByFileUrl(writeFile.getFileUrl()))).stream(inputStream, (long)inputStream.available(), -1L).build());
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | MinioException e) {
            throw new FileOperationException("文件写入失败", e);
        }

    }
}
