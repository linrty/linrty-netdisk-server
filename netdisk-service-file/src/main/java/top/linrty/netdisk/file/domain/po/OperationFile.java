package top.linrty.netdisk.file.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationFile {

    private String extendName;

    private String fileUrl;

    private Range range;

    private long fileSize;
}
