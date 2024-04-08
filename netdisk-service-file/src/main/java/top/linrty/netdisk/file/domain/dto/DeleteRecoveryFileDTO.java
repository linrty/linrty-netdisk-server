package top.linrty.netdisk.file.domain.dto;

import lombok.Data;

@Data
// @Schema(name = "删除回收文件DTO",required = true)
public class DeleteRecoveryFileDTO {
    // @Schema(description = "用户文件id", required = true)
    private String userFileId;

}
