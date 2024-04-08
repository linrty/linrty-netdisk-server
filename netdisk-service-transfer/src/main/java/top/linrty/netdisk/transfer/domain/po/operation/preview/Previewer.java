package top.linrty.netdisk.transfer.domain.po.operation.preview;

import cn.hutool.http.HttpUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import top.linrty.netdisk.common.config.ThumbImageConfig;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.CharsetUtils;
import top.linrty.netdisk.common.util.FileOperation;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.common.util.ImageUtil;
import top.linrty.netdisk.transfer.domain.po.operation.preview.entity.PreviewFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Data
@NoArgsConstructor
public abstract class Previewer {

    public ThumbImageConfig thumbImageConfig;

    protected abstract InputStream getInputStream(PreviewFile previewFile);

    public void imageThumbnailPreview(HttpServletResponse httpServletResponse, PreviewFile previewFile) {
        String fileUrl = previewFile.getFileUrl();
        String thumbnailImgUrl;
        ServletOutputStream outputStream;
        InputStream inputstream;
        // 判断是否是http/https开头的url,这一段表示
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            // 判断是否是视频文件
            boolean isVideo = FileTypeUtil.isVideoFile(FilenameUtils.getExtension(fileUrl));
            // 获取缩略图url
            thumbnailImgUrl = previewFile.getFileUrl();
            if (isVideo) {
                // 视频文件缩略图url
                thumbnailImgUrl = fileUrl.replace("." + FilenameUtils.getExtension(fileUrl), ".jpg");
            }
            // 获取缓存文件
            File cacheFile = FileTypeUtil.getCacheFile(thumbnailImgUrl);
            // 缓存文件存在
            if (cacheFile.exists()) {
                FileInputStream fis = null;
                outputStream = null;

                try {
                    // 读取缓存文件
                    fis = new FileInputStream(cacheFile);
                    // 获取输出流
                    outputStream = httpServletResponse.getOutputStream();
                    // 将缓存文件写入输出流
                    IOUtils.copy(fis, outputStream);
                } catch (IOException var63) {
                    var63.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(fis);
                    IOUtils.closeQuietly(outputStream);
                }
            } else {
                InputStream in = null;
                outputStream = null;

                try {
                    inputstream = this.getInputStream(previewFile);
                } catch (FileOperationException e) {
                    log.error(e.getMessage());
                    throw new FileOperationException("获取文件流失败");
                }

                try {
                    outputStream = httpServletResponse.getOutputStream();
                    in = ImageUtil.thumbnailsImageForScale(inputstream, cacheFile, 50L);
                    IOUtils.copy(in, outputStream);
                } catch (IOException e) {
                    throw new FileOperationException("获取文件流失败");
                } finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(inputstream);
                    IOUtils.closeQuietly(outputStream);
                    if (previewFile.getOssClient() != null) {
                        previewFile.getOssClient().shutdown();
                    }

                }
            }

        } else {
            // 是以http/https开头的url
            String[] arr = fileUrl.replace("http://", "").replace("https://", "").split("/");
            // 获取缩略图url
            thumbnailImgUrl = arr[0];
            // 获取icoUrl
            String icoUrl = findIco(fileUrl);
            // 获取缓存文件
            File cacheFile = FileTypeUtil.getCacheFile(FileTypeUtil.getUploadFileUrl(thumbnailImgUrl, "ico"));
            FileInputStream fis;
            if (cacheFile.exists()) {
                fis = null;
                outputStream = null;

                try {
                    fis = new FileInputStream(cacheFile);
                    outputStream = httpServletResponse.getOutputStream();
                    IOUtils.copy(fis, outputStream);
                } catch (IOException e) {
                    throw new FileOperationException("获取文件流失败");
                } finally {
                    IOUtils.closeQuietly(fis);
                    IOUtils.closeQuietly(outputStream);
                }
            } else {
                fis = null;
                inputstream = null;
                outputStream = null;

                try {
                    URL url = new URL(icoUrl);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    inputstream = connection.getInputStream();

                    try {
                        outputStream = httpServletResponse.getOutputStream();
                        inputstream = ImageUtil.thumbnailsImageForScale(inputstream, cacheFile, 50L);
                        IOUtils.copy(inputstream, outputStream);
                    } catch (IOException e) {
                        throw new FileOperationException("获取文件流失败");
                    } finally {
                        IOUtils.closeQuietly(inputstream);
                        IOUtils.closeQuietly(inputstream);
                        IOUtils.closeQuietly(outputStream);
                        if (previewFile.getOssClient() != null) {
                            previewFile.getOssClient().shutdown();
                        }

                    }
                } catch (MalformedURLException e) {
                    log.error("MalformedURLException, url is {}", icoUrl);
                    throw new FileOperationException("MalformedURLException", e);
                } catch (IOException e) {
                    log.error("IOException, url is {}", icoUrl);
                    throw new FileOperationException("IO流获取异常", e);
                }
            }

        }
    }

    public void imageOriginalPreview(HttpServletResponse httpServletResponse, PreviewFile previewFile) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = this.getInputStream(previewFile);
            outputStream = httpServletResponse.getOutputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            bytes = CharsetUtils.convertTxtCharsetToUTF8(bytes, FilenameUtils.getExtension(previewFile.getFileUrl()));
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new FileOperationException("IO流获取异常");
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            if (previewFile.getOssClient() != null) {
                previewFile.getOssClient().shutdown();
            }

        }

    }

    private static String findIco(String navUrl) {
        String body = HttpUtil.createGet(navUrl).execute().toString();
        String str = body.split("favicon\\d{0,3}.ico")[0];
        int http = str.indexOf("https://", str.length() - 100);
        if (http == -1) {
            http = str.indexOf("http://", str.length() - 100);
        }

        if (http == -1) {
            int i = navUrl.indexOf("/", 8);
            if (i > 0) {
                navUrl = navUrl.substring(0, i);
            }
        } else {
            navUrl = str.substring(http);
        }

        return navUrl + "/favicon.ico";
    }
}