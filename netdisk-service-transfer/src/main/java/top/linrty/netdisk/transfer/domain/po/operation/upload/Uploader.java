package top.linrty.netdisk.transfer.domain.po.operation.upload;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import top.linrty.netdisk.transfer.domain.po.NetdiskMultipartFile;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.RedisLock;
import top.linrty.netdisk.common.util.RedisUtil;
import top.linrty.netdisk.transfer.domain.po.operation.upload.entity.UploadFileChunk;
import top.linrty.netdisk.common.domain.vo.transfer.UploadFileResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * 上传器
 */
@Slf4j
@NoArgsConstructor
public abstract class Uploader {
    @Resource
    RedisLock redisLock;
    @Resource
    RedisUtil redisUtil;


    // 只发送了一个请求没有发送文件
    public List<UploadFileResult> upload(HttpServletRequest httpServletRequest) {
        UploadFileChunk uploadFileChunk = new UploadFileChunk();
        uploadFileChunk.setChunkIndex(1);
        uploadFileChunk.setChunkSize(0L);
        uploadFileChunk.setTotalChunks(1);
        uploadFileChunk.setIdentifier(UUID.randomUUID().toString());
        return this.upload(httpServletRequest, uploadFileChunk);
    }

    /**
     * 上传文件
     * @param httpServletRequest
     * @param uploadFileChunk
     * @return
     */
    public List<UploadFileResult> upload(HttpServletRequest httpServletRequest, UploadFileChunk uploadFileChunk) {
        List<UploadFileResult> uploadFileResultList = new ArrayList();
        StandardMultipartHttpServletRequest request = (StandardMultipartHttpServletRequest)httpServletRequest;
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            throw new FileOperationException("未包含文件上传域");
        } else {
            try {
                Iterator<String> iter = request.getFileNames();

                while(iter.hasNext()) {
                    List<MultipartFile> multipartFileList = request.getFiles((String)iter.next());
                    Iterator var8 = multipartFileList.iterator();

                    while(var8.hasNext()) {
                        MultipartFile multipartFile = (MultipartFile)var8.next();
                        NetdiskMultipartFile netdiskMultipartFile = new NetdiskMultipartFile(multipartFile);
                        UploadFileResult uploadFileResult = this.doUploadFlow(netdiskMultipartFile, uploadFileChunk);
                        uploadFileResultList.add(uploadFileResult);
                    }
                }

                return uploadFileResultList;
            } catch (Exception e) {
                throw new FileOperationException("上传文件失败", e);
            }
        }
    }

    /**
     * 执行上传流程
     * @param netdiskMultipartFile
     * @param uploadFileChunk
     * @return
     */
    protected UploadFileResult doUploadFlow(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        try {
            this.rectifier(netdiskMultipartFile, uploadFileChunk);
            UploadFileResult uploadFileResult = this.organizationalResults(netdiskMultipartFile, uploadFileChunk);
            return uploadFileResult;
        } catch (Exception e) {
            throw new FileOperationException("上传文件失败", e);
        }
    }

    /**
     * 取消上传
     * @param uploadFileChunk
     */
    public abstract void cancelUpload(UploadFileChunk uploadFileChunk);

    /**
     * 上传文件块
     * @param netdiskMultipartFile
     * @param uploadFileChunk
     * @throws IOException
     */
    protected abstract void doUploadFileChunk(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) throws IOException;


    /**
     * 组织上传结果
     * @param netdiskMultipartFile
     * @param uploadFileChunk
     * @return
     */
    protected abstract UploadFileResult organizationalResults(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk);

    /**
     * 校正
     * @param netdiskMultipartFile
     * @param uploadFileChunk
     */
    private void rectifier(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        String key = "NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":lock";
        String current_upload_chunk_number = "NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":current_upload_chunk_number";
        this.redisLock.lock(key);

        try {
            if (this.redisUtil.getObject(current_upload_chunk_number) == null) {
                this.redisUtil.set(current_upload_chunk_number, "1", 3600000L);
            }

            int currentUploadChunkNumber = Integer.parseInt(this.redisUtil.getObject(current_upload_chunk_number));
            if (uploadFileChunk.getChunkIndex() != currentUploadChunkNumber) {
                this.redisLock.unlock(key);
                Thread.sleep(100L);

                while(this.redisLock.tryLock(key, 300L, TimeUnit.SECONDS)) {
                    currentUploadChunkNumber = Integer.parseInt(this.redisUtil.getObject(current_upload_chunk_number));
                    if (uploadFileChunk.getChunkIndex() <= currentUploadChunkNumber) {
                        break;
                    }

                    if (Math.abs(currentUploadChunkNumber - uploadFileChunk.getChunkIndex()) > 2) {
                        log.error("传入的切片数据异常，当前应上传切片为第{}块，传入的为第{}块。", currentUploadChunkNumber, uploadFileChunk.getChunkIndex());
                        throw new FileOperationException("传入的切片数据异常");
                    }

                    this.redisLock.unlock(key);
                }
            }

            log.info("文件名{},正在上传第{}块, 共{}块>>>>>>>>>>", new Object[]{netdiskMultipartFile.getMultipartFile().getOriginalFilename(), uploadFileChunk.getChunkIndex(), uploadFileChunk.getTotalChunks()});
            if (uploadFileChunk.getChunkIndex() == currentUploadChunkNumber) {
                this.doUploadFileChunk(netdiskMultipartFile, uploadFileChunk);
                log.info("文件名{},第{}块上传成功", netdiskMultipartFile.getMultipartFile().getOriginalFilename(), uploadFileChunk.getChunkIndex());
                this.redisUtil.getIncr("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":current_upload_chunk_number");
            }
        } catch (Exception e) {
            log.error("第{}块上传失败，自动重试", uploadFileChunk.getChunkIndex());
            this.redisUtil.set("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":current_upload_chunk_number", String.valueOf(uploadFileChunk.getChunkIndex()), 3600000L);
            throw new FileOperationException("更新远程文件出错", e);
        } finally {
            this.redisLock.unlock(key);
        }

    }

    /**
     * 检查上传状态
     * @param param
     * @param confFile
     * @return
     * @throws IOException
     */
    public synchronized boolean checkUploadStatus(UploadFileChunk param, File confFile) throws IOException {
        RandomAccessFile confAccessFile = new RandomAccessFile(confFile, "rw");

        try {
            // 设置文件长度
            confAccessFile.setLength((long)param.getTotalChunks());
            // 设置文件指针位置
            confAccessFile.seek((long)(param.getChunkIndex() - 1));
            // 写入数据
            confAccessFile.write(127);
        }catch (Exception e){
            throw new FileOperationException("写入数据失败", e);
        }finally {
            IOUtils.closeQuietly(confAccessFile);
        }

        byte[] var4 = FileUtils.readFileToByteArray(confFile);
        byte[] var5 = var4;
        int var6 = var4.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            byte b = var5[var7];
            if (b != 127) {
                return false;
            }
        }
        confFile.delete();
        return true;
    }

    /**
     * 写入文件
     * @param fileData
     * @param file
     * @param uploadFileChunk
     */
    public void writeByteDataToFile(byte[] fileData, File file, UploadFileChunk uploadFileChunk) {
        try {
            // 以读写方式打开文件
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            // 获取文件通道
            FileChannel fileChannel = raf.getChannel();
            // 设置文件通道的位置
            long position = (long)(uploadFileChunk.getChunkIndex() - 1) * uploadFileChunk.getChunkSize();
            // 将字节数据写入文件
            fileChannel.position(position);
            // 将字节数据包装成缓冲区
            fileChannel.write(ByteBuffer.wrap(fileData));
            // 强制刷新到磁盘
            fileChannel.force(true);
            // 关闭文件通道
            fileChannel.close();
            // 关闭文件
            raf.close();
        } catch (IOException e) {
            throw new FileOperationException("写入数据失败", e);
        }
    }
}
