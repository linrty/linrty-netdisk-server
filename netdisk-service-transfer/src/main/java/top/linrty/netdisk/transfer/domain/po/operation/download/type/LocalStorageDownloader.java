package top.linrty.netdisk.transfer.domain.po.operation.download.type;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.download.Downloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.entity.DownloadFile;

import java.io.*;

@NoArgsConstructor
@Slf4j
@Component
public class LocalStorageDownloader extends Downloader {

    @Override
    public void prepareDownload() {

    }

    @Override
    public InputStream getInputStream(DownloadFile downloadFile) {
        File file = new File(FileTypeUtil.getStaticPath() + downloadFile.getFileUrl());
        InputStream inputStream = null;
        byte[] bytes = new byte[0];
        RandomAccessFile randowAccessFile = null;

        try {
            if (downloadFile.getRange() != null) {
                randowAccessFile = new RandomAccessFile(file, "r");
                randowAccessFile.seek(downloadFile.getRange().getStart());
                bytes = new byte[downloadFile.getRange().getLength()];
                randowAccessFile.read(bytes);
            } else {
                inputStream = new FileInputStream(file);
                bytes = IOUtils.toByteArray(inputStream);
            }
        } catch (IOException e) {
            throw new FileOperationException("读取文件失败");
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(randowAccessFile);
        }

        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void doDownloadFinish() {

    }
}