package top.linrty.netdisk.file.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "分享列表DTO",required = true)
public class ShareListDTO {
    // @Schema(description="分享文件路径")
    private String shareFilePath;
    // @Schema(description="批次号")
    private String shareBatchNum;
    // @Schema(description = "当前页码")
    private Long currentPage;
    // @Schema(description = "一页显示数量")
    private Long pageCount;
}
