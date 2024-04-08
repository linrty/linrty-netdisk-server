package top.linrty.netdisk.transfer.domain.po.operation.write.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteFile {

    private String fileUrl;

    private long fileSize;

}
