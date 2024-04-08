package top.linrty.netdisk.transfer.domain.po.operation.upload.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 文件分片信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileChunk {
    // 分片索引
    private int chunkIndex;
    // 分片大小
    private long chunkSize;
    // 分片总数量
    private int totalChunks;
    // 文件标识
    private String identifier;
    // 文件总大小
    private long totalSize;
    // 当前分片大小
    private long currentChunkSize;
}
