package top.linrty.netdisk.file.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "批量移动文件DTO",required = true)
public class BatchMoveFileDTO {
    // @Schema(description="用户文件Id集合", required = true)
    private String userFileIds;
    // @Schema(description="目的文件路径", required = true)
    private String filePath;


}
