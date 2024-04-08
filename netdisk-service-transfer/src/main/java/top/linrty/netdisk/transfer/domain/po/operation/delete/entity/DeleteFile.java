package top.linrty.netdisk.transfer.domain.po.operation.delete.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteFile {

    private String fileUrl;

}