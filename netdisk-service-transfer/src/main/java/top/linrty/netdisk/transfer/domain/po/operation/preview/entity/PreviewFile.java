package top.linrty.netdisk.transfer.domain.po.operation.preview.entity;

import com.aliyun.oss.OSS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreviewFile {

    private String fileUrl;

    private OSS ossClient;

}
