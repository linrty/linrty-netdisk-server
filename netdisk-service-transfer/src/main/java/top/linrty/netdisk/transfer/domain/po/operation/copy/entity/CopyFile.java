package top.linrty.netdisk.transfer.domain.po.operation.copy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CopyFile {
    private String extendName;
}
