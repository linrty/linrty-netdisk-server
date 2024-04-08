package top.linrty.netdisk.transfer.domain.dto;

import lombok.Data;

@Data
//@Schema(name = "下载文件DTO",required = true)
public class DownloadFileDTO {

    private String userFileId;

    // @Schema(description="批次号")
    private String shareBatchNum;

    // @Schema(description="提取码")
    private String extractionCode;
}
