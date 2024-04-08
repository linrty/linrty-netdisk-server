package top.linrty.netdisk.file.domain.dto;

import lombok.Data;
import top.linrty.netdisk.common.constant.Constants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class CreateFileDTO {

    // @Schema(description = "文件路径", required = true)
    private String filePath;

    // @Schema(description = "文件名", required = true)
    @NotBlank(message = "文件名不能为空")
    @Pattern(regexp = Constants.FILE_NAME_REGEX, message = "文件名不合法！", flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String fileName;

    // @Schema(description = "扩展名", required = true)
    private String extendName;

}
