package top.linrty.netdisk.transfer.domain.po.operation.download.entity;

import com.aliyun.oss.OSS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.linrty.netdisk.transfer.domain.po.Range;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadFile {

    private String fileUrl;

    private OSS ossClient;

    private Range range;
}
