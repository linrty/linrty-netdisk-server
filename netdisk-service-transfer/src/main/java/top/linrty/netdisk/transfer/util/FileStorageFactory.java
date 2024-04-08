package top.linrty.netdisk.transfer.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.config.MinioConfig;
import top.linrty.netdisk.common.config.ThumbImageConfig;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.transfer.domain.po.operation.copy.Copier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.type.AliyunOSSCopier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.type.LocalStorageCopier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.type.MinioCopier;
import top.linrty.netdisk.transfer.domain.po.operation.delete.Deleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.type.AliyunOSSDeleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.type.LocalStorageDeleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.type.MinioDeleter;
import top.linrty.netdisk.transfer.domain.po.operation.download.Downloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.type.AliyunOSSDownloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.type.LocalStorageDownloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.type.MinioDownloader;
import top.linrty.netdisk.transfer.domain.po.operation.preview.Previewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.type.AliyunOSSPreviewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.type.LocalStoragePreviewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.type.MinioPreviewer;
import top.linrty.netdisk.transfer.domain.po.operation.read.Reader;
import top.linrty.netdisk.transfer.domain.po.operation.read.type.AliyunOSSReader;
import top.linrty.netdisk.transfer.domain.po.operation.read.type.LocalStorageReader;
import top.linrty.netdisk.transfer.domain.po.operation.read.type.MinioReader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.Uploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.type.AliyunOSSUploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.type.LocalStorageUploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.type.MinioUploader;
import top.linrty.netdisk.transfer.domain.po.operation.write.Writer;
import top.linrty.netdisk.transfer.domain.po.operation.write.type.AliyunOSSWriter;
import top.linrty.netdisk.transfer.domain.po.operation.write.type.LocalStorageWriter;
import top.linrty.netdisk.transfer.domain.po.operation.write.type.MinioWriter;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class FileStorageFactory {

    @Value("${netdisk.storage-type}")
    // 存储类型
    private String storageType;

    // 阿里云配置
    private AliyunConfig aliyunConfig;

    // 缩略图配置
    private ThumbImageConfig thumbImageConfig;

    // Minio配置
    private MinioConfig minioConfig;

    // 七牛云配置
    // private QiniuyunConfig qiniuyunConfig;



    public Uploader getUploader() {
        int type = Integer.parseInt(this.storageType);
        Uploader uploader = null;
        if (StorageTypeEnum.LOCAL.getCode() == type) {
            uploader = new LocalStorageUploader();
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == type) {
            uploader = new AliyunOSSUploader(this.aliyunConfig);
        } else if (StorageTypeEnum.MINIO.getCode() == type) {
            uploader = new MinioUploader(this.minioConfig);
        }

        return uploader;
    }

    public Downloader getDownloader(int storageType) {
        Downloader downloader = null;
        if (StorageTypeEnum.LOCAL.getCode() == storageType) {
            downloader = new LocalStorageDownloader();
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == storageType) {
            downloader = AliyunOSSDownloader.builder()
                    .aliyunConfig(this.aliyunConfig).build();
        } else if (StorageTypeEnum.MINIO.getCode() == storageType) {
            downloader = MinioDownloader.builder()
                    .minioConfig(this.minioConfig).build();
        }

        return downloader;
    }

    public Deleter getDeleter(int storageType) {
        Deleter deleter = null;
        if (StorageTypeEnum.LOCAL.getCode() == storageType) {
            deleter = new LocalStorageDeleter();
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == storageType) {
            deleter = new AliyunOSSDeleter(this.aliyunConfig);
        } else if (StorageTypeEnum.MINIO.getCode() == storageType) {
            deleter = new MinioDeleter(this.minioConfig);
        }
        return deleter;
    }

    public Reader getReader(int storageType) {
        Reader reader = null;
        if (StorageTypeEnum.LOCAL.getCode() == storageType) {
            reader = new LocalStorageReader();
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == storageType) {
            reader = new AliyunOSSReader(this.aliyunConfig);
        } else if (StorageTypeEnum.MINIO.getCode() == storageType) {
            reader = new MinioReader(this.minioConfig);
        }
        return reader;
    }

    public Writer getWriter(int storageType) {
        Writer writer = null;
        if (StorageTypeEnum.LOCAL.getCode() == storageType) {
            writer = new LocalStorageWriter();
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == storageType) {
            writer = new AliyunOSSWriter(this.aliyunConfig);
        } else if (StorageTypeEnum.MINIO.getCode() == storageType) {
            writer = new MinioWriter(this.minioConfig);
        }

        return writer;
    }


    public Previewer getPreviewer(int storageType) {
        Previewer previewer = null;
        if (StorageTypeEnum.LOCAL.getCode() == storageType) {
            previewer = new LocalStoragePreviewer(this.thumbImageConfig);
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == storageType) {
            previewer = new AliyunOSSPreviewer(this.aliyunConfig, this.thumbImageConfig);
        } else if (StorageTypeEnum.MINIO.getCode() == storageType) {
            previewer = new MinioPreviewer(this.minioConfig, this.thumbImageConfig);
        }

        return previewer;
    }

    public Copier getCopier() {
        int type = Integer.parseInt(this.storageType);
        Copier copier = null;
        if (StorageTypeEnum.LOCAL.getCode() == type) {
            copier = new LocalStorageCopier();
        } else if (StorageTypeEnum.ALIYUN_OSS.getCode() == type) {
            copier = new AliyunOSSCopier(this.aliyunConfig);
        }else if (StorageTypeEnum.MINIO.getCode() == type) {
            copier = new MinioCopier(this.minioConfig);
        }

        return copier;
    }


}
