package top.linrty.netdisk.transfer.domain.po.operation.delete.type;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.delete.Deleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.entity.DeleteFile;

import java.io.File;

@Slf4j
@NoArgsConstructor
@Component
public class LocalStorageDeleter extends Deleter {

    public void delete(DeleteFile deleteFile) {
        File localSaveFile = FileTypeUtil.getLocalSaveFile(deleteFile.getFileUrl());
        if (localSaveFile.exists()) {
            boolean result = localSaveFile.delete();
            if (!result) {
                throw new FileOperationException("删除本地文件失败");
            }
        }

        this.deleteCacheFile(deleteFile);
    }
}
