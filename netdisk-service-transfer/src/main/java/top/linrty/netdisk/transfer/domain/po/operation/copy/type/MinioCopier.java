package top.linrty.netdisk.transfer.domain.po.operation.copy.type;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.copy.Copier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.entity.CopyFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Component
@Slf4j
public class MinioCopier extends Copier {

    private MinioConfig minioConfig;

    public String copy(InputStream inputStream, CopyFile copyFile) {
        String uuid = UUID.randomUUID().toString();
        String fileUrl = FileTypeUtil.getUploadFileUrl(uuid, copyFile.getExtendName());

        try {
            MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
            boolean isExist = minioClient.bucketExists((BucketExistsArgs)((BucketExistsArgs.Builder)BucketExistsArgs.builder().bucket(this.minioConfig.getBucketName())).build());
            if (!isExist) {
                minioClient.makeBucket((MakeBucketArgs)((MakeBucketArgs.Builder)MakeBucketArgs.builder().bucket(this.minioConfig.getBucketName())).build());
            }

            minioClient.putObject((PutObjectArgs)((PutObjectArgs.Builder)((PutObjectArgs.Builder)PutObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(fileUrl)).stream(inputStream, (long)inputStream.available(), 5242880L).build());
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | MinioException e) {
            throw new FileOperationException("文件操作异常", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return fileUrl;
    }
}
