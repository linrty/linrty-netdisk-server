package top.linrty.netdisk.common.domain.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "预览文件DTO",required = true)
public class PreviewDTO {

    @Schema(description = "用户文件id")
    private String userFileId;

    @Schema(description="批次号")
    private String shareBatchNum;

    @Schema(description="提取码")
    private String extractionCode;

    private String isMin;

    private Integer platform;

    private String url;

    private String token;
}
