package top.linrty.netdisk.transfer.domain.po.operation.upload.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFileInfo {
    private String bucketName;
    private String key;
    private String uploadId;
}
