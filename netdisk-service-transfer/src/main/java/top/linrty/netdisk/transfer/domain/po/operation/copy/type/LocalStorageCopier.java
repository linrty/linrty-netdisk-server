package top.linrty.netdisk.transfer.domain.po.operation.copy.type;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.copy.Copier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.entity.CopyFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@NoArgsConstructor
@Slf4j
@Component
public class LocalStorageCopier extends Copier {

    public String copy(InputStream inputStream, CopyFile copyFile) {
        String uuid = UUID.randomUUID().toString();
        String fileUrl = FileTypeUtil.getUploadFileUrl(uuid, copyFile.getExtendName());
        File saveFile = new File(FileTypeUtil.getStaticPath() + fileUrl);

        try {
            FileUtils.copyInputStreamToFile(inputStream, saveFile);
        } catch (IOException e) {
            throw new FileOperationException("文件操作异常", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return fileUrl;
    }
}
