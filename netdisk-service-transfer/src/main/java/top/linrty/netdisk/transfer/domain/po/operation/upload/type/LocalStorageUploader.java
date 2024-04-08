package top.linrty.netdisk.transfer.domain.po.operation.upload.type;

import cn.hutool.core.util.StrUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.transfer.domain.po.NetdiskMultipartFile;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.operation.upload.Uploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.entity.UploadFileChunk;
import top.linrty.netdisk.transfer.domain.vo.UploadFileResult;
import top.linrty.netdisk.transfer.enums.UploadFileStatusEnum;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@NoArgsConstructor
public class LocalStorageUploader extends Uploader {
    public static Map<String, String> FILE_URL_MAP = new HashMap();

    protected UploadFileResult doUploadFlow(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        UploadFileResult uploadFileResult = new UploadFileResult();

        try {
            String fileUrl = FileTypeUtil.getUploadFileUrl(uploadFileChunk.getIdentifier(), netdiskMultipartFile.getExtendName());
            if (StrUtil.isNotEmpty((CharSequence)FILE_URL_MAP.get(uploadFileChunk.getIdentifier()))) {
                fileUrl = (String)FILE_URL_MAP.get(uploadFileChunk.getIdentifier());
            } else {
                FILE_URL_MAP.put(uploadFileChunk.getIdentifier(), fileUrl);
            }

            String tempFileUrl = fileUrl + "_tmp";
            String confFileUrl = fileUrl.replace("." + netdiskMultipartFile.getExtendName(), ".conf");
            File file = new File(FileTypeUtil.getStaticPath() + fileUrl);
            File tempFile = new File(FileTypeUtil.getStaticPath() + tempFileUrl);
            File confFile = new File(FileTypeUtil.getStaticPath() + confFileUrl);
            RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");

            try {
                FileChannel fileChannel = raf.getChannel();
                long position = (long)(uploadFileChunk.getChunkIndex() - 1) * uploadFileChunk.getChunkSize();
                byte[] fileData = netdiskMultipartFile.getUploadBytes();
                fileChannel.position(position);
                fileChannel.write(ByteBuffer.wrap(fileData));
                fileChannel.force(true);
                fileChannel.close();
            }catch (Exception e){
                throw new FileOperationException("文件上传失败", e);
            }finally {
                IOUtils.closeQuietly(raf);
            }

            boolean isComplete = this.checkUploadStatus(uploadFileChunk, confFile);
            uploadFileResult.setFileUrl(fileUrl);
            uploadFileResult.setFileName(netdiskMultipartFile.getFileName());
            uploadFileResult.setExtendName(netdiskMultipartFile.getExtendName());
            uploadFileResult.setFileSize(uploadFileChunk.getTotalSize());
            uploadFileResult.setStorageType(StorageTypeEnum.LOCAL);
            if (uploadFileChunk.getTotalChunks() == 1) {
                uploadFileResult.setFileSize(netdiskMultipartFile.getSize());
            }

            uploadFileResult.setIdentifier(uploadFileChunk.getIdentifier());
            if (isComplete) {
                tempFile.renameTo(file);
                FILE_URL_MAP.remove(uploadFileChunk.getIdentifier());
                if (FileTypeUtil.isImageFile(uploadFileResult.getExtendName())) {
                    InputStream is = null;

                    try {
                        is = new FileInputStream(FileTypeUtil.getLocalSaveFile(fileUrl));
                        BufferedImage src = ImageIO.read(is);
                        uploadFileResult.setBufferedImage(src);
                    } catch (IOException e) {
                        throw new FileOperationException("文件保存失败", e);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }

                uploadFileResult.setStatus(UploadFileStatusEnum.SUCCESS);
            } else {
                uploadFileResult.setStatus(UploadFileStatusEnum.UNCOMPLATE);
            }

            return uploadFileResult;
        } catch (IOException e) {
            throw new FileOperationException("文件上传失败", e);
        }
    }

    public void cancelUpload(UploadFileChunk uploadFileChunk) {
        String fileUrl = (String)FILE_URL_MAP.get(uploadFileChunk.getIdentifier());
        String tempFileUrl = fileUrl + "_tmp";
        String confFileUrl = fileUrl.replace("." + FilenameUtils.getExtension(fileUrl), ".conf");
        File tempFile = new File(tempFileUrl);
        if (tempFile.exists()) {
            tempFile.delete();
        }

        File confFile = new File(confFileUrl);
        if (confFile.exists()) {
            confFile.delete();
        }

    }

    protected void doUploadFileChunk(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
    }

    protected UploadFileResult organizationalResults(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        return null;
    }
}
