package top.linrty.netdisk.transfer.domain.po.operation.preview.type;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.ThumbImageConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.preview.Previewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.entity.PreviewFile;

import java.io.*;

@Slf4j
@NoArgsConstructor
@Component
public class LocalStoragePreviewer extends Previewer {

    public LocalStoragePreviewer(ThumbImageConfig thumbImageConfig) {
        this.setThumbImageConfig(thumbImageConfig);
    }

    protected InputStream getInputStream(PreviewFile previewFile) {
        File file = FileTypeUtil.getLocalSaveFile(previewFile.getFileUrl());
        if (!file.exists()) {
            throw new FileOperationException("[FileTypeUtil] Failed to get the file stream because the file path does not exist! The file path is: " + file.getAbsolutePath());
        } else {
            InputStream inputStream = null;
            byte[] bytes = new byte[0];

            try {
                inputStream = new FileInputStream(file);
                bytes = IOUtils.toByteArray(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new FileOperationException("文件不存在");
            } catch (IOException e) {
                throw new FileOperationException("读取文件失败");
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            return new ByteArrayInputStream(bytes);
        }
    }
}
