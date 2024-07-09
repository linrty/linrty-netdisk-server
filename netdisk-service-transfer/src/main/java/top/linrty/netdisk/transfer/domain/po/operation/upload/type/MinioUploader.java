package top.linrty.netdisk.transfer.domain.po.operation.upload.type;

import io.minio.*;
import io.minio.errors.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.NetdiskMultipartFile;
import top.linrty.netdisk.common.util.RedisUtil;
import top.linrty.netdisk.transfer.domain.po.operation.upload.Uploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.entity.UploadFileChunk;
import top.linrty.netdisk.common.domain.vo.transfer.UploadFileResult;
import top.linrty.netdisk.common.enums.UploadFileStatusEnum;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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
public class MinioUploader extends Uploader {

    private MinioConfig minioConfig;

    @Resource
    RedisUtil redisUtil;


    public MinioUploader(MinioConfig minioConfig) {
        this.minioConfig = minioConfig;
    }

    public void cancelUpload(UploadFileChunk uploadFileChunk) {
        // TODO 实现Minio的取消上传
    }

    protected void doUploadFileChunk(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        // TODO 实现Minio的上传文件分片
    }

    protected UploadFileResult organizationalResults(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        // TODO 实现Minio的组织结果
        return null;
    }

    protected UploadFileResult doUploadFlow(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        UploadFileResult uploadFileResult = new UploadFileResult();

        try {
            netdiskMultipartFile.getFileUrl(uploadFileChunk.getIdentifier());
            String fileUrl = FileTypeUtil.getUploadFileUrl(uploadFileChunk.getIdentifier(), netdiskMultipartFile.getExtendName());
            File tempFile = FileTypeUtil.getTempFile(fileUrl);
            File processFile = FileTypeUtil.getProcessFile(fileUrl);
            byte[] fileData = netdiskMultipartFile.getUploadBytes();
            this.writeByteDataToFile(fileData, tempFile, uploadFileChunk);
            boolean isComplete = this.checkUploadStatus(uploadFileChunk, processFile);
            uploadFileResult.setFileUrl(fileUrl);
            uploadFileResult.setFileName(netdiskMultipartFile.getFileName());
            uploadFileResult.setExtendName(netdiskMultipartFile.getExtendName());
            uploadFileResult.setFileSize(uploadFileChunk.getTotalSize());
            uploadFileResult.setStorageType(StorageTypeEnum.MINIO);
            if (uploadFileChunk.getTotalChunks() == 1) {
                uploadFileResult.setFileSize(netdiskMultipartFile.getSize());
            }

            uploadFileResult.setIdentifier(uploadFileChunk.getIdentifier());
            if (isComplete) {
                this.minioUpload(fileUrl, tempFile, uploadFileChunk);
                uploadFileResult.setFileUrl(fileUrl);
                tempFile.delete();
                if (FileTypeUtil.isImageFile(uploadFileResult.getExtendName())) {
                    InputStream inputStream = null;

                    try {
                        MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
                        inputStream = minioClient.getObject((GetObjectArgs)((GetObjectArgs.Builder)((GetObjectArgs.Builder)GetObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(uploadFileResult.getFileUrl())).build());
                        BufferedImage src = ImageIO.read(inputStream);
                        uploadFileResult.setBufferedImage(src);
                    } catch (InternalException | XmlParserException | InvalidResponseException | InvalidKeyException |
                             NoSuchAlgorithmException | ErrorResponseException | InsufficientDataException | ServerException |
                             IOException var16) {
                        var16.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                    }
                }

                uploadFileResult.setStatus(UploadFileStatusEnum.SUCCESS);
            } else {
                uploadFileResult.setStatus(UploadFileStatusEnum.UNCOMPLATE);
            }

            return uploadFileResult;
        } catch (IOException e) {
            throw new FileOperationException("文件上传失败", e);
        }
    }

    private void minioUpload(String fileUrl, File file, UploadFileChunk uploadFileChunk) {
        InputStream inputStream = null;

        try {
            MinioClient minioClient = MinioClient.builder().endpoint(this.minioConfig.getEndpoint()).credentials(this.minioConfig.getAccessKey(), this.minioConfig.getSecretKey()).build();
            boolean isExist = minioClient.bucketExists((BucketExistsArgs)((BucketExistsArgs.Builder)BucketExistsArgs.builder().bucket(this.minioConfig.getBucketName())).build());
            if (!isExist) {
                minioClient.makeBucket((MakeBucketArgs)((MakeBucketArgs.Builder)MakeBucketArgs.builder().bucket(this.minioConfig.getBucketName())).build());
            }

            inputStream = new FileInputStream(file);
            minioClient.putObject((PutObjectArgs)((PutObjectArgs.Builder)((PutObjectArgs.Builder)PutObjectArgs.builder().bucket(this.minioConfig.getBucketName())).object(fileUrl)).stream(inputStream, uploadFileChunk.getTotalSize(), 5242880L).build());
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException | MinioException e) {
            throw new FileOperationException("文件上传失败", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

    }
}
