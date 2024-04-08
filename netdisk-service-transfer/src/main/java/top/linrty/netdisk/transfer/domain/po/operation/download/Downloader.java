package top.linrty.netdisk.transfer.domain.po.operation.download;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import top.linrty.netdisk.transfer.domain.po.operation.download.entity.DownloadFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@NoArgsConstructor
public abstract class Downloader {
    /**
     * 执行下载流程
     * 公共步骤
     */
    public void download(HttpServletResponse httpServletResponse, DownloadFile downloadFile) {
        // 0. 执行准备工作
        this.prepareDownload();
        // 1. 获取输入流
        InputStream inputStream = this.getInputStream(downloadFile);
        // 2. 获取输出流
        ServletOutputStream outputStream = null;
        try {
            // 3. 获取输出流
            outputStream = httpServletResponse.getOutputStream();
            // 4. 将输入流拷贝到输出流
            IOUtils.copyLarge(inputStream, outputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            // 执行收尾工作
            // 5. 关闭输入流和输出流
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            // 6. 执行各个子类的收尾工作
            this.doDownloadFinish();
        }

    }

    public abstract void prepareDownload();


    public abstract InputStream getInputStream(DownloadFile downloadFile);


    public abstract void doDownloadFinish();

}