package top.linrty.netdisk.transfer.domain.po.operation.delete.type;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.transfer.domain.po.operation.delete.Deleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.entity.DeleteFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MinioDeleter extends Deleter {

    private MinioConfig minioConfig;

    public void delete(DeleteFile deleteFile) {
        try {
            MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
            minioClient.removeObject((RemoveObjectArgs)((RemoveObjectArgs.Builder)((RemoveObjectArgs.Builder)RemoveObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(deleteFile.getFileUrl())).build());
            log.info("successfully removed mybucket/myobject");
        } catch (MinioException e) {
            log.error("Error: " + e);
            throw new FileOperationException("Minio删除文件失败", e);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new FileOperationException("Minio删除文件失败", e);
        }

        this.deleteCacheFile(deleteFile);
    }
}
