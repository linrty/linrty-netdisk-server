package top.linrty.netdisk.transfer.domain.po.operation.read.type;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.common.util.ReadFileUtil;
import top.linrty.netdisk.transfer.domain.po.operation.read.Reader;
import top.linrty.netdisk.transfer.domain.po.operation.read.entity.ReadFile;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Setter
@Getter
@Builder
@NoArgsConstructor
@Component
public class LocalStorageReader extends Reader {

    public String read(ReadFile readFile) {
        FileInputStream fileInputStream = null;

        String fileContent;
        try {
            String extendName = FilenameUtils.getExtension(readFile.getFileUrl());
            fileInputStream = new FileInputStream(FileTypeUtil.getStaticPath() + readFile.getFileUrl());
            fileContent = ReadFileUtil.getContentByInputStream(extendName, fileInputStream);
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return fileContent;
    }
}
