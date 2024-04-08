package top.linrty.netdisk.file.domain.vo;

import lombok.Data;

@Data
// @Schema(description="分享文件VO")
public class ShareFileVO {
    // @Schema(description="批次号")
    private String shareBatchNum;
    // @Schema(description = "提取编码")
    private String extractionCode;
}
