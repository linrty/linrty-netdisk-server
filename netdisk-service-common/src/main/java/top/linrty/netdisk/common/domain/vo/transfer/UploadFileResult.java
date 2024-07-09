package top.linrty.netdisk.common.domain.vo.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.common.enums.UploadFileStatusEnum;

import java.awt.image.BufferedImage;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "上传文件结果", required = true)
public class UploadFileResult {

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件扩展名")
    private String extendName;

    @Schema(description = "文件大小")
    private long fileSize;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件标识")
    private String identifier;

    @Schema(description = "存储类型")
    private StorageTypeEnum storageType;

    @Schema(description = "文件上传状态")
    private UploadFileStatusEnum status;

    @Schema(description = "图片缓冲流")
    private BufferedImage bufferedImage;
}
