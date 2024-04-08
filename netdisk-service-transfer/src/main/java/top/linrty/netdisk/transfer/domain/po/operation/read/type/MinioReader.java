package top.linrty.netdisk.transfer.domain.po.operation.read.type;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.ReadFileUtil;
import top.linrty.netdisk.transfer.domain.po.operation.read.Reader;
import top.linrty.netdisk.transfer.domain.po.operation.read.entity.ReadFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class MinioReader extends Reader {

    private MinioConfig minioConfig;
    public String read(ReadFile readFile) {
        String fileUrl = readFile.getFileUrl();
        String fileType = FilenameUtils.getExtension(fileUrl);

        try {
            return ReadFileUtil.getContentByInputStream(fileType, this.getInputStream(readFile.getFileUrl()));
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败", e);
        }
    }

    protected InputStream getInputStream(String fileUrl) {
        InputStream inputStream = null;

        try {
            MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
            inputStream = minioClient.getObject((GetObjectArgs)((GetObjectArgs.Builder)((GetObjectArgs.Builder)GetObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(fileUrl)).build());
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            throw new FileOperationException("MiniO异常", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            throw new FileOperationException("获取IO流异常", e);
        }

        return inputStream;
    }
}
