package top.linrty.netdisk.common.domain.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "移动文件DTO", required = true)
public class MoveFileDTO {

    @Schema(description = "用户文件id", required = true)
    private String userFileId;

    @Schema(description = "文件路径", required = true)
    private String filePath;
//
//    @Schema(description = "文件名", required = true)
//    private String fileName;
//
//    @Schema(description = "旧文件名", required = true)
//    private String oldFilePath;
//    @Schema(description = "扩展名", required = true)
//    private String extendName;

}
