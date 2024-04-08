package top.linrty.netdisk.transfer.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.transfer.enums.UploadFileStatusEnum;

import java.awt.image.BufferedImage;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileResult {

    // 文件名
    private String fileName;

    // 文件扩展名
    private String extendName;

    // 文件大小
    private long fileSize;

    // 文件URL
    private String fileUrl;

    // 文件标识
    private String identifier;

    // 存储类型
    private StorageTypeEnum storageType;

    // 文件上传状态
    private UploadFileStatusEnum status;

    // 图片缓冲流
    private BufferedImage bufferedImage;
}
