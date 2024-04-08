package top.linrty.netdisk.file.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "回收文件DTO",required = true)
public class RestoreFileDTO {
    // @Schema(description="删除批次号")
    private String deleteBatchNum;
    // @Schema(description="文件路径")
    private String filePath;
}
