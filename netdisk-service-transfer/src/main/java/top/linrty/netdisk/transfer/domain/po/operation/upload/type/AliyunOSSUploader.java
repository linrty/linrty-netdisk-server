package top.linrty.netdisk.transfer.domain.po.operation.upload.type;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import top.linrty.netdisk.common.config.AliyunConfig;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.AliyunUtil;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.transfer.domain.po.NetdiskMultipartFile;
import top.linrty.netdisk.common.util.RedisUtil;
import top.linrty.netdisk.transfer.domain.po.operation.upload.Uploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.entity.UploadFileChunk;
import top.linrty.netdisk.transfer.domain.po.operation.upload.entity.UploadFileInfo;
import top.linrty.netdisk.transfer.domain.vo.UploadFileResult;
import top.linrty.netdisk.transfer.enums.UploadFileStatusEnum;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AliyunOSSUploader extends Uploader {
    @Resource
    RedisUtil redisUtil;

    private AliyunConfig aliyunConfig;

    public AliyunOSSUploader(AliyunConfig aliyunConfig) {
        this.aliyunConfig = aliyunConfig;
    }

    /**
     * 上传文件分片
     * @param netdiskMultipartFile
     * @param uploadFileChunk
     * @throws IOException
     */
    @Override
    protected void doUploadFileChunk(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) throws IOException {
        // 根据配置获取OSS客户端
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);

        try {
            UploadFileInfo uploadFileInfo = (UploadFileInfo) JSON.parseObject(this.redisUtil.getObject("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":uploadPartRequest"), UploadFileInfo.class);
            String fileUrl = netdiskMultipartFile.getFileUrl();
            if (uploadFileInfo == null) {
                // 初始化分片上传
                InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(this.aliyunConfig.getBucketName(), fileUrl);
                InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
                String uploadId = upresult.getUploadId();
                uploadFileInfo = new UploadFileInfo();
                uploadFileInfo.setBucketName(this.aliyunConfig.getBucketName());
                uploadFileInfo.setKey(fileUrl);
                uploadFileInfo.setUploadId(uploadId);
                this.redisUtil.set("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":uploadPartRequest", JSON.toJSONString(uploadFileInfo));
            }
            // 上传分片
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(uploadFileInfo.getBucketName());
            uploadPartRequest.setKey(uploadFileInfo.getKey());
            uploadPartRequest.setUploadId(uploadFileInfo.getUploadId());
            uploadPartRequest.setInputStream(netdiskMultipartFile.getUploadInputStream());
            uploadPartRequest.setPartSize(netdiskMultipartFile.getSize());
            uploadPartRequest.setPartNumber(uploadFileChunk.getChunkIndex());
            log.debug(JSON.toJSONString(uploadPartRequest));
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            log.debug("上传结果：" + JSON.toJSONString(uploadPartResult));
            if (this.redisUtil.hasKey("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":partETags")) {
                List<PartETag> partETags = JSON.parseArray(this.redisUtil.getObject("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":partETags"), PartETag.class, new JSONReader.Feature[0]);
                partETags.add(uploadPartResult.getPartETag());
                this.redisUtil.set("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":partETags", JSON.toJSONString(partETags));
            } else {
                List<PartETag> partETags = new ArrayList();
                partETags.add(uploadPartResult.getPartETag());
                this.redisUtil.set("NetdiskUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":partETags", JSON.toJSONString(partETags));
            }
        }catch (Exception e){
            throw new FileOperationException("上传文件分片失败", e);
        }finally {
            ossClient.shutdown();
        }

    }

    /**
     * 组织上传结果
     * @param netdiskMultipartFile
     * @param uploadFileChunk
     * @return
     */
    @Override
    protected UploadFileResult organizationalResults(NetdiskMultipartFile netdiskMultipartFile, UploadFileChunk uploadFileChunk) {
        UploadFileResult uploadFileResult = new UploadFileResult();
        UploadFileInfo uploadFileInfo = (UploadFileInfo)JSON.parseObject(this.redisUtil.getObject("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":uploadPartRequest"), UploadFileInfo.class);
        uploadFileResult.setFileUrl(uploadFileInfo.getKey());
        uploadFileResult.setFileName(netdiskMultipartFile.getFileName());
        uploadFileResult.setExtendName(netdiskMultipartFile.getExtendName());
        uploadFileResult.setFileSize(uploadFileChunk.getTotalSize());
        if (uploadFileChunk.getTotalChunks() == 1) {
            uploadFileResult.setFileSize(netdiskMultipartFile.getSize());
        }

        uploadFileResult.setStorageType(StorageTypeEnum.ALIYUN_OSS);
        uploadFileResult.setIdentifier(uploadFileChunk.getIdentifier());
        if (uploadFileChunk.getChunkIndex() == uploadFileChunk.getTotalChunks()) {
            log.info("分片上传完成");
            this.completeMultipartUpload(uploadFileChunk);
            this.redisUtil.deleteKey("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":current_upload_chunk_number");
            this.redisUtil.deleteKey("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":partETags");
            this.redisUtil.deleteKey("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":uploadPartRequest");
            if (FileTypeUtil.isImageFile(uploadFileResult.getExtendName())) {
                OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
                OSSObject ossObject = ossClient.getObject(this.aliyunConfig.getBucketName(), FileTypeUtil.getAliyunObjectNameByFileUrl(uploadFileResult.getFileUrl()));
                InputStream is = ossObject.getObjectContent();

                try {
                    BufferedImage src = ImageIO.read(is);
                    uploadFileResult.setBufferedImage(src);
                } catch (IOException e) {
                    throw new FileOperationException("获取图片流失败", e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }

            uploadFileResult.setStatus(UploadFileStatusEnum.SUCCESS);
        } else {
            uploadFileResult.setStatus(UploadFileStatusEnum.UNCOMPLATE);
        }

        return uploadFileResult;
    }

    /**
     * 完成分片上传
     * @param uploadFileChunk
     */
    private void completeMultipartUpload(UploadFileChunk uploadFileChunk) {
        List<PartETag> partETags = JSON.parseArray(this.redisUtil.getObject("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":partETags"), PartETag.class, new JSONReader.Feature[0]);
        partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));
        UploadFileInfo uploadFileInfo = (UploadFileInfo)JSON.parseObject(this.redisUtil.getObject("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":uploadPartRequest"), UploadFileInfo.class);
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(this.aliyunConfig.getBucketName(), uploadFileInfo.getKey(), uploadFileInfo.getUploadId(), partETags);
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        ossClient.shutdown();
    }

    /**
     * 取消上传
     * @param uploadFileChunk
     */
    @Override
    public void cancelUpload(UploadFileChunk uploadFileChunk) {
        UploadFileInfo uploadFileInfo = (UploadFileInfo)JSON.parseObject(this.redisUtil.getObject("QiwenUploader:Identifier:" + uploadFileChunk.getIdentifier() + ":uploadPartRequest"), UploadFileInfo.class);
        OSS ossClient = AliyunUtil.getOSSClient(this.aliyunConfig);
        AbortMultipartUploadRequest abortMultipartUploadRequest = new AbortMultipartUploadRequest(this.aliyunConfig.getBucketName(), uploadFileInfo.getKey(), uploadFileInfo.getUploadId());
        ossClient.abortMultipartUpload(abortMultipartUploadRequest);
        ossClient.shutdown();
    }
}