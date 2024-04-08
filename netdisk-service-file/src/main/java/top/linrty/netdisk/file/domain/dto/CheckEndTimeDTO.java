package top.linrty.netdisk.file.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "校验过期时间DTO",required = true)
public class CheckEndTimeDTO {
    // @Schema(description="批次号")
    private String shareBatchNum;

}