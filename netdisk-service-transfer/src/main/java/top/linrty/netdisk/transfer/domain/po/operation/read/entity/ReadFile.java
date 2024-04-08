package top.linrty.netdisk.transfer.domain.po.operation.read.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadFile {

    private String fileUrl;

}
