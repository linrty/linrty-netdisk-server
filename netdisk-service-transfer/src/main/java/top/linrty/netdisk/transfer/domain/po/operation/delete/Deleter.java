package top.linrty.netdisk.transfer.domain.po.operation.delete;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.delete.entity.DeleteFile;

import java.io.File;

@Slf4j
@NoArgsConstructor
public abstract class Deleter {
    public abstract void delete(DeleteFile deleteFile);

    protected void deleteCacheFile(DeleteFile deleteFile) {
        if (FileTypeUtil.isImageFile(FilenameUtils.getExtension(deleteFile.getFileUrl()))) {
            File cacheFile = FileTypeUtil.getCacheFile(deleteFile.getFileUrl());
            if (cacheFile.exists()) {
                boolean result = cacheFile.delete();
                if (!result) {
                    log.error("删除本地缓存文件失败！");
                }
            }
        }

    }
}
