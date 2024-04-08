package top.linrty.netdisk.transfer.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "预览文件DTO",required = true)
public class PreviewDTO {

    private String userFileId;

    // @Schema(description="批次号")
    private String shareBatchNum;

    //@Schema(description="提取码")
    private String extractionCode;

    private String isMin;

    private Integer platform;

    private String url;

    private String token;
}
