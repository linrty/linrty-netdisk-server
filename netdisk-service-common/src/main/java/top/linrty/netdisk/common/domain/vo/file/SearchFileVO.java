package top.linrty.netdisk.common.domain.vo.file;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchFileVO {
    private String userFileId;
    private String fileName;
    private String filePath;
    private String extendName;
    private Long fileSize;
    private String fileUrl;
    private Map<String, List<String>> highLight;
    private Integer isDir;
}
