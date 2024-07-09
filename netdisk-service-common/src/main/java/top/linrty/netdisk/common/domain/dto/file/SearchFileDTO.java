package top.linrty.netdisk.common.domain.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "搜索文件DTO", required = true)
public class SearchFileDTO {

    @Schema(description = "文件名", required = true)
    private String fileName;

    @Schema(description = "当前页", required = true)
    private long currentPage;

    @Schema(description = "每页数量", required = true)
    private long pageCount;
}
