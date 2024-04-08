package top.linrty.netdisk.file.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "校验提取码DTO",required = true)
public class CheckExtractionCodeDTO {
    // @Schema(description="批次号")
    private String shareBatchNum;
    // @Schema(description="提取码")
    private String extractionCode;




}