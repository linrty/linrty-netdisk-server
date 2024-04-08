package top.linrty.netdisk.transfer.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.net.multipart.UploadFile;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestParam;
import top.linrty.netdisk.common.constant.MQConstants;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.enums.StorageTypeEnum;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.*;
import top.linrty.netdisk.transfer.api.FileClient;
import top.linrty.netdisk.transfer.domain.dto.DownloadFileDTO;
import top.linrty.netdisk.transfer.domain.dto.PreviewDTO;
import top.linrty.netdisk.transfer.domain.dto.UploadFileChunkDTO;
import top.linrty.netdisk.transfer.domain.po.UploadTask;
import top.linrty.netdisk.transfer.domain.po.UploadTaskDetail;
import top.linrty.netdisk.transfer.domain.po.operation.copy.Copier;
import top.linrty.netdisk.transfer.domain.po.operation.copy.entity.CopyFile;
import top.linrty.netdisk.transfer.domain.po.operation.delete.Deleter;
import top.linrty.netdisk.transfer.domain.po.operation.delete.entity.DeleteFile;
import top.linrty.netdisk.transfer.domain.po.operation.download.Downloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.entity.DownloadFile;
import top.linrty.netdisk.transfer.domain.po.operation.preview.Previewer;
import top.linrty.netdisk.transfer.domain.po.operation.preview.entity.PreviewFile;
import top.linrty.netdisk.transfer.domain.po.operation.upload.Uploader;
import top.linrty.netdisk.transfer.domain.po.operation.upload.entity.UploadFileChunk;
import top.linrty.netdisk.transfer.domain.vo.UploadFileResult;
import top.linrty.netdisk.transfer.domain.vo.UploadFileVO;
import top.linrty.netdisk.transfer.enums.UploadFileStatusEnum;
import top.linrty.netdisk.transfer.mapper.UploadTaskDetailMapper;
import top.linrty.netdisk.transfer.mapper.UploadTaskMapper;
import top.linrty.netdisk.transfer.service.IFileTransferService;
import top.linrty.netdisk.transfer.util.FileStorageFactory;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class FileTransferService implements IFileTransferService {

    @Resource
    FileClient fileClient;

    @Resource
    RabbitMqHelper rabbitMqHelper;

    @Resource
    FileStorageFactory fileStorageFactory;

    @Resource
    UploadTaskDetailMapper uploadTaskDetailMapper;

    @Resource
    UploadTaskMapper uploadTaskMapper;

    /**
     * 秒传文件
     * @param uploadFileChunkDTO
     * @return
     */
    @Override
    @GlobalTransactional
    public UploadFileVO uploadFileSpeed(UploadFileChunkDTO uploadFileChunkDTO) {
        boolean isCheckSuccess = fileClient.checkStorage(uploadFileChunkDTO.getTotalSize());

        if (!isCheckSuccess) {
             throw new FileOperationException("存储空间不足");
        }

        UploadFileVO uploadFileVO = new UploadFileVO();

        // 初始化一个netdiskFile对象用于存储文件信息
        String filePath = uploadFileChunkDTO.getFilePath();
        String relativePath = uploadFileChunkDTO.getRelativePath();
        NetdiskFile netdiskFile = null;
        System.out.println("filePath:"+filePath);
        System.out.println("fileName:"+uploadFileChunkDTO.getFilename());
        System.out.println("relativePath:"+relativePath);
        if (relativePath.contains("/")) {
            netdiskFile = new NetdiskFile(filePath, relativePath, false);
        } else {
            netdiskFile = new NetdiskFile(filePath, uploadFileChunkDTO.getFilename(), false);
        }

        // 先尝试直接添加文件指针
        if(fileClient.addFilePoint(uploadFileChunkDTO.getIdentifier(), netdiskFile.getPath(), netdiskFile.isDirectory())){
            // 处理文件夹
            if(relativePath.contains("/")){
                Map<String, String> map = new HashMap<>();
                // map.put("netdiskFile", JSON.toJSONString(netdiskFile));
                map.put("path", netdiskFile.getPath());
                map.put("isDir", netdiskFile.isDirectory()? "1": "0");
                map.put("userId", UserContext.getUser());
                rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL,
                        MQConstants.ROUTING_KEY_RESTORE_PARENT,
                        map);
            }
            uploadFileVO.setSkipUpload(true);
        }else {
            uploadFileVO.setSkipUpload(false);
            List<Integer> uploaded = uploadTaskDetailMapper.selectUploadedChunkNumList(uploadFileChunkDTO.getIdentifier());
            if (uploaded != null && !uploaded.isEmpty()) {
                uploadFileVO.setUploaded(uploaded);
            } else {

                LambdaQueryWrapper<UploadTask> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UploadTask::getIdentifier, uploadFileChunkDTO.getIdentifier());
                List<UploadTask> rslist = uploadTaskMapper.selectList(lambdaQueryWrapper);
                if (rslist == null || rslist.isEmpty()) {
                    UploadTask uploadTask = new UploadTask();
                    uploadTask.setIdentifier(uploadFileChunkDTO.getIdentifier());
                    uploadTask.setUploadTime(DateUtil.now());
                    uploadTask.setUploadStatus(UploadFileStatusEnum.UNCOMPLATE.getCode());
                    uploadTask.setFileName(netdiskFile.getNameNotExtend());
                    uploadTask.setFilePath(netdiskFile.getParent());
                    uploadTask.setExtendName(netdiskFile.getExtendName());
                    uploadTask.setUserId(UserContext.getUser());
                    uploadTaskMapper.insert(uploadTask);
                }
            }
        }
        // 直接返回添加的结果
        return uploadFileVO;
    }

    /**
     * 正常上传文件
     * @param request
     * @param uploadFileChunkDto
     * @param userId
     */
    @Override
    @GlobalTransactional
    public void uploadFile(HttpServletRequest request, UploadFileChunkDTO uploadFileChunkDto, String userId) {
        UploadFileChunk uploadFileChunk = new UploadFileChunk();
        uploadFileChunk.setChunkIndex(uploadFileChunkDto.getChunkNumber());
        uploadFileChunk.setChunkSize(uploadFileChunkDto.getChunkSize());
        uploadFileChunk.setTotalChunks(uploadFileChunkDto.getTotalChunks());
        uploadFileChunk.setIdentifier(uploadFileChunkDto.getIdentifier());
        uploadFileChunk.setTotalSize(uploadFileChunkDto.getTotalSize());
        uploadFileChunk.setCurrentChunkSize(uploadFileChunkDto.getCurrentChunkSize());

        // 获取上传器
        Uploader uploader = fileStorageFactory.getUploader();
        if (uploader == null) {
            log.error("上传失败，请检查storageType是否配置正确");
            throw new FileOperationException("上传失败");
        }

        // 上传文件
        List<UploadFileResult> uploadFileResultList;
        try {
            uploadFileResultList = uploader.upload(request, uploadFileChunk);
        } catch (Exception e) {
            log.error("上传失败，请检查UFOP连接配置是否正确");
            throw new FileOperationException("上传失败", e);
        }

        for (int i = 0; i < uploadFileResultList.size(); i++){
            UploadFileResult uploadFileResult = uploadFileResultList.get(i);
            String relativePath = uploadFileChunkDto.getRelativePath();
            NetdiskFile netdiskFile = null;
            if (relativePath.contains("/")) {
                netdiskFile = new NetdiskFile(uploadFileChunkDto.getFilePath(), relativePath, false);
            } else {
                netdiskFile = new NetdiskFile(uploadFileChunkDto.getFilePath(), uploadFileChunkDto.getFilename(), false);
            }

            if (UploadFileStatusEnum.SUCCESS.equals(uploadFileResult.getStatus())){
                // 文件上传成功

                // 1.向file表中插入文件信息
                String fileId = fileClient.addFile(uploadFileResult.getFileUrl(),
                        uploadFileResult.getFileSize(),
                        uploadFileResult.getStorageType().getCode(),
                        uploadFileResult.getIdentifier());

                // 2.向user_file表中插入用户文件信息
                fileClient.addUserFile(netdiskFile.getPath(), netdiskFile.isDirectory(), fileId, uploadFileChunkDto.getIdentifier());

                // 3.处理文件夹
                if (relativePath.contains("/")) {
                    Map<String, String> map = new HashMap<>();
                    // map.put("netdiskFile", JSON.toJSONString(netdiskFile));
                    map.put("path", netdiskFile.getPath());
                    map.put("isDir", netdiskFile.isDirectory()? "1": "0");
                    map.put("userId", userId);
                    rabbitMqHelper.sendMessage(
                            MQConstants.EXCHANGE_FILE_DEAL,
                            MQConstants.ROUTING_KEY_RESTORE_PARENT,
                            map);
                }

                // 4. 删除上传任务
                LambdaQueryWrapper<UploadTaskDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UploadTaskDetail::getIdentifier, uploadFileChunkDto.getIdentifier());
                uploadTaskDetailMapper.delete(lambdaQueryWrapper);

                // 5. 更新上传任务状态
                LambdaUpdateWrapper<UploadTask> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.set(UploadTask::getUploadStatus, UploadFileStatusEnum.SUCCESS.getCode())
                        .eq(UploadTask::getIdentifier, uploadFileChunkDto.getIdentifier());
                uploadTaskMapper.update(null, lambdaUpdateWrapper);

                // 6. 如果文件是图片文件，生成图片缩略图
                if (FileTypeUtil.isImageFile(uploadFileResult.getExtendName())) {
                    BufferedImage src = uploadFileResult.getBufferedImage();
                    fileClient.genImage(fileId, src.getWidth(), src.getHeight());
                }

                // 7. 如果文件是音乐文件，解析音乐文件
                if (FileTypeUtil.isMusicFile(uploadFileResult.getExtendName())) {
                    fileClient.parseMusic(uploadFileResult.getExtendName(), uploadFileResult.getStorageType().getCode(), uploadFileResult.getFileUrl(), fileId);
                }

            } else if (UploadFileStatusEnum.UNCOMPLATE.equals(uploadFileResult.getStatus())) {
                // 上传未完成
                // TODO 共享上传进度
                UploadTaskDetail uploadTaskDetail = new UploadTaskDetail();
                uploadTaskDetail.setFilePath(netdiskFile.getParent());
                uploadTaskDetail.setFilename(netdiskFile.getNameNotExtend());
                uploadTaskDetail.setChunkNumber(uploadFileChunkDto.getChunkNumber());
                uploadTaskDetail.setChunkSize((int) uploadFileChunkDto.getChunkSize());
                uploadTaskDetail.setRelativePath(uploadFileChunkDto.getRelativePath());
                uploadTaskDetail.setTotalChunks(uploadFileChunkDto.getTotalChunks());
                uploadTaskDetail.setTotalSize((int) uploadFileChunkDto.getTotalSize());
                uploadTaskDetail.setIdentifier(uploadFileChunkDto.getIdentifier());
                uploadTaskDetailMapper.insert(uploadTaskDetail);

            } else if (UploadFileStatusEnum.FAIL.equals(uploadFileResult.getStatus())) {
                // 上传失败
                LambdaQueryWrapper<UploadTaskDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UploadTaskDetail::getIdentifier, uploadFileChunkDto.getIdentifier());
                uploadTaskDetailMapper.delete(lambdaQueryWrapper);

                LambdaUpdateWrapper<UploadTask> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.set(UploadTask::getUploadStatus, UploadFileStatusEnum.FAIL.getCode())
                        .eq(UploadTask::getIdentifier, uploadFileChunkDto.getIdentifier());
                uploadTaskMapper.update(null, lambdaUpdateWrapper);
            }
        }
    }

    /**
     * 下载文件
     * @param httpServletResponse
     * @param downloadFileDTO
     */
    @Override
    public void downloadFile(HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO) {

        Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(downloadFileDTO.getUserFileId());
        if (userFileInfo.get("isDir").equals(0)) {
            // 文件
            Map<String, Object>fileInfo = fileClient.getFileInfoMap((String) userFileInfo.get("fileId"));
            Downloader downloader = fileStorageFactory.getDownloader((Integer) fileInfo.get("storageType"));
            if (downloader == null) {
                log.error("下载失败，文件存储类型不支持下载，storageType:{}", fileInfo.get("storageType"));
                throw new FileOperationException("下载失败");
            }
            DownloadFile downloadFile = new DownloadFile();
            downloadFile.setFileUrl((String) fileInfo.get("fileUrl"));
            // 这里不能转成（Long）的原因
            httpServletResponse.setContentLengthLong((Integer) fileInfo.get("fileSize"));
            downloader.download(httpServletResponse, downloadFile);
        } else {
            // 文件夹
            String filePath = (String) userFileInfo.get("filePath");
            String fileName = (String) userFileInfo.get("fileName");
            // 找出文件夹下的所有文件
            NetdiskFile netdiskFile = new NetdiskFile(filePath,fileName, true);
            List<String> userFileIds = fileClient.getDirChildren(netdiskFile.getPath() ,(String) userFileInfo.get("userId"));
            // 下载这个文件夹下的所有文件
            downloadUserFileList(httpServletResponse, filePath, fileName, userFileIds);
        }
    }

    /**
     * 下载文件夹下的所有文件
     * @param httpServletResponse
     * @param filePath
     * @param fileName
     * @param userFileIds
     */
    @Override
    public void downloadUserFileList(HttpServletResponse httpServletResponse, String filePath, String fileName, List<String> userFileIds) {
        // 创建临时文件夹
        String staticPath = FileTypeUtil.getStaticPath();
        String tempPath = staticPath + "temp" + File.separator;
        File tempDirFile = new File(tempPath);
        if (!tempDirFile.exists()) {
            tempDirFile.mkdirs();
        }
        // 将所有文件打包成zip文件
        FileOutputStream f = null;
        try {
            f = new FileOutputStream(tempPath + fileName + ".zip");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        CheckedOutputStream csum = new CheckedOutputStream(f, new Adler32());
        ZipOutputStream zos = new ZipOutputStream(csum);
        BufferedOutputStream out = new BufferedOutputStream(zos);


        try {
            for (String userFileId : userFileIds) {
                // 遍历所有文件，获取文件信息
                Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(userFileId);

                String fileId = (String) userFileInfo.get("fileId");
                String userFileFilePath = (String) userFileInfo.get("filePath");
                String userFileFileName = (String) userFileInfo.get("fileName");
                String userFileExtendName = (String) userFileInfo.get("extendName");

                if (userFileInfo.get("isDir").equals(0)) {
                    // 如果是文件
                    // 获取文件信息
                    Map<String, Object> fileInfo = fileClient.getFileInfoMap(fileId);
                    Integer storageType = (Integer) fileInfo.get("storageType");

                    Downloader downloader = fileStorageFactory.getDownloader(storageType);
                    if (downloader == null) {
                        log.error("下载失败，文件存储类型不支持下载，storageType:{}", storageType);
                        throw new FileOperationException("下载失败");
                    }
                    DownloadFile downloadFile = new DownloadFile();
                    downloadFile.setFileUrl((String) fileInfo.get("fileUrl"));
                    InputStream inputStream = downloader.getInputStream(downloadFile);
                    BufferedInputStream bis = new BufferedInputStream(inputStream);

                    try {
                        NetdiskFile netdiskFile = new NetdiskFile(StrUtil.removePrefix(userFileFilePath, filePath), userFileFileName + "." + userFileExtendName, false);
                        // 将文件打包进zip
                        zos.putNextEntry(new ZipEntry(netdiskFile.getPath()));

                        byte[] buffer = new byte[1024];
                        int i = bis.read(buffer);
                        while (i != -1) {
                            out.write(buffer, 0, i);
                            i = bis.read(buffer);
                        }
                    } catch (IOException e) {
                        log.error("" + e);
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(bis);
                        try {
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // 如果是文件夹

                    NetdiskFile netdiskFile = new NetdiskFile(StrUtil.removePrefix(userFileFilePath, filePath), userFileFileName, true);
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(netdiskFile.getPath() + NetdiskFile.separator));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            }

        } catch (Exception e) {
            log.error("压缩过程中出现异常:"+ e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String zipPath = "";
        try {
            Downloader downloader = fileStorageFactory.getDownloader(StorageTypeEnum.LOCAL.getCode());
            DownloadFile downloadFile = new DownloadFile();
            downloadFile.setFileUrl("temp" + File.separator + fileName + ".zip");
            File tempFile = new File(FileTypeUtil.getStaticPath() + downloadFile.getFileUrl());
            httpServletResponse.setContentLengthLong(tempFile.length());
            downloader.download(httpServletResponse, downloadFile);
            zipPath = FileTypeUtil.getStaticPath() + "temp" + File.separator + fileName + ".zip";
        } catch (Exception e) {
            //org.apache.catalina.connector.ClientAbortException: java.io.IOException: Connection reset by peer
            if (e.getMessage().contains("ClientAbortException")) {
                //该异常忽略不做处理
            } else {
                log.error("下传zip文件出现异常：{}", e.getMessage());
            }

        } finally {
            File file = new File(zipPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 预览文件
     * @param httpServletResponse
     * @param previewDTO
     */

    @Override
    public void previewFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO) {
        Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(previewDTO.getUserFileId());
        Map<String, Object> fileInfo = fileClient.getFileInfoMap((String) userFileInfo.get("fileId"));

        Integer storageType = (Integer) fileInfo.get("storageType");
        String fileUrl = (String) fileInfo.get("fileUrl");

        Previewer previewer = fileStorageFactory.getPreviewer(storageType);
        if (previewer == null) {
            log.error("预览失败，文件存储类型不支持预览，storageType:{}", storageType);
            throw new FileOperationException("预览失败");
        }
        PreviewFile previewFile = new PreviewFile();
        previewFile.setFileUrl(fileUrl);
        try {
            if ("true".equals(previewDTO.getIsMin())) {
                previewer.imageThumbnailPreview(httpServletResponse, previewFile);
            } else {
                previewer.imageOriginalPreview(httpServletResponse, previewFile);
            }
        } catch (Exception e){
            //org.apache.catalina.connector.ClientAbortException: java.io.IOException: 你的主机中的软件中止了一个已建立的连接。
            if (e.getMessage().contains("ClientAbortException")) {
                //该异常忽略不做处理
            } else {
                log.error("预览文件出现异常：{}", e.getMessage());
            }
            throw new FileOperationException("预览失败");
        }
    }

    /**
     * 预览图片文件
     * @param httpServletResponse
     * @param previewDTO
     */
    @Override
    public void previewPictureFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO) {
        // 获取图片文件信息
        Map<String, Object> pictureFileInfo = fileClient.getPictureFileInfoMapByFileUrl(previewDTO.getUrl());

        Integer storageType = (Integer) pictureFileInfo.get("storageType");
        String fileUrl = (String) pictureFileInfo.get("fileUrl");
        String extendName = (String) pictureFileInfo.get("extendName");
        String fileName = (String) pictureFileInfo.get("fileName");

        // 根据存储类型获取预览器
        Previewer previewer = fileStorageFactory.getPreviewer(storageType);
        if (previewer == null) {
            log.error("预览失败，文件存储类型不支持预览，storageType:{}", storageType);
            throw new FileOperationException("预览失败");
        }
        PreviewFile previewFile = new PreviewFile();
        previewFile.setFileUrl(fileUrl);
        //  previewFile.setFileSize(pictureFile.getFileSize());
        try {

            String mime= MimeUtils.getMime(extendName);
            httpServletResponse.setHeader("Content-Type", mime);

            String pictureFileName = fileName + "." + extendName;
            try {
                pictureFileName = new String(pictureFileName.getBytes("utf-8"), "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            httpServletResponse.addHeader("Content-Disposition", "fileName=" + pictureFileName);// 设置文件名

            previewer.imageOriginalPreview(httpServletResponse, previewFile);
        } catch (Exception e){
            //org.apache.catalina.connector.ClientAbortException: java.io.IOException: 你的主机中的软件中止了一个已建立的连接。
            if (e.getMessage().contains("ClientAbortException")) {
                //该异常忽略不做处理
            } else {
                log.error("预览文件出现异常：{}", e.getMessage());
            }

        }
    }

    /**
     * 删除文件
     * @param fileId
     */
    @Override
    @GlobalTransactional
    public Boolean deleteFile(String fileId) {
        Map<String, Object> fileInfo = fileClient.getFileInfoMap(fileId);

        Integer storageType = (Integer) fileInfo.get("storageType");
        String fileUrl = (String) fileInfo.get("fileUrl");

        // 获取删除器
        Deleter deleter = fileStorageFactory.getDeleter(storageType);
        if (deleter == null) {
            log.error("删除失败，文件存储类型不支持删除，storageType:{}", storageType);
            return false;
            // throw new FileOperationException("删除失败");
        }

        DeleteFile deleteFile = new DeleteFile();
        deleteFile.setFileUrl(fileUrl);
        deleter.delete(deleteFile);

        return true;
    }


    @Override
    @GlobalTransactional
    public Boolean deleteFileToRecovery(String userFileId) {
        if (fileClient.deleteFile(userFileId)){
            rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL
                    , MQConstants.ROUTING_KEY_DELETE_USER_FILE_FROM_ES, userFileId);
            return true;
        }
        throw new FileOperationException("删除失败");
    }

    @Override
    public String copyFile(String Url, String targetFilePath, String extendName) {
        String url2 = ClassUtils.getDefaultClassLoader().getResource(Url).getPath();
        try {
            url2 = URLDecoder.decode(url2, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new FileOperationException("解码失败");
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(url2);
        } catch (FileNotFoundException e) {
            throw new FileOperationException("模板文件复制失败");
        }
        Copier copier = fileStorageFactory.getCopier();
        CopyFile copyFile = new CopyFile();
        copyFile.setExtendName(extendName);
        return copier.copy(fileInputStream, copyFile);
    }


    @Override
    @GlobalTransactional
    public void copyFile(String filePath, String[] userFileIds) {
        for (String userFileId : userFileIds) {
            Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(userFileId);
            String oldFilePath = (String) userFileInfo.get("filePath");
            String fileName = (String) userFileInfo.get("fileName");
            if (userFileInfo.get("isDir").equals(1)) {
                NetdiskFile netdiskFile = new NetdiskFile(oldFilePath, fileName, true);
                if (filePath.startsWith(netdiskFile.getPath() + NetdiskFile.separator)
                        || filePath.equals(netdiskFile.getPath())) {
                    throw new FileOperationException("原路径与目标路径冲突，不能复制");
                }
            }
            fileClient.copyUserFile(userFileId, filePath);
            // 删除重复的文件夹
            fileClient.deleteRepeatSubDirFile(filePath);
        }
    }


    @Override
    public String downloadFile2Temp(String fileId) {
        Map<String, Object> fileInfo = fileClient.getFileInfoMap(fileId);
        String destFileUrl = FileTypeUtil.getStaticPath() + "temp" + File.separator + (String) fileInfo.get("fileUrl");
        File destFile = new File(destFileUrl);
        Downloader downloader = fileStorageFactory.getDownloader((Integer) fileInfo.get("storageType"));
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setFileUrl((String) fileInfo.get("fileUrl"));
        InputStream inputStream = downloader.getInputStream(downloadFile);
        try {
            FileUtils.copyInputStreamToFile(inputStream, destFile);
        } catch (IOException e) {
            throw new FileOperationException("文件下载失败");
        }
        return destFileUrl;
    }

    @Override
    public Map<String, Object> uploadTempFile2Netdisk(String fileUrl, String destUrl) {
        File destFile = new File(destUrl);
        Downloader downloader = fileStorageFactory.getDownloader(Integer.parseInt(fileStorageFactory.getStorageType()));
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setFileUrl(fileUrl.replace(FileTypeUtil.getStaticPath(), ""));
        InputStream inputStream = downloader.getInputStream(downloadFile);
        try {
            FileUtils.copyInputStreamToFile(inputStream, destFile);
        } catch (IOException e) {
            throw new FileOperationException("文件下载失败");
        }
        Map<String, Object> map = new HashMap<>();

        System.out.println("size:"+destFile.length());
        map.put("fileSize", destFile.length());
        return map;
    }


    @Override
    public List<String> unzipFile(String unzipFileTempUrl, String extendName) {
        // 解压到同级目录的同名文件夹下
        String targetUnzipDirUrl = unzipFileTempUrl.replace("." + extendName, "");

        File unzipFile = new File(unzipFileTempUrl);

        List<String> fileEntryNameList = new ArrayList<>();

        try {
            fileEntryNameList = FileOperation.unzip(unzipFile, targetUnzipDirUrl);
        } catch (Exception e) {
            log.error("解压失败" + e);
            throw new FileOperationException("解压异常：" + e.getMessage());
        }
        return fileEntryNameList;
    }

    @Override
    public String getMd5(String fileUrl) {
        File entryFile = new File(fileUrl);
        FileInputStream fis = null;
        String md5Str = UUID.randomUUID().toString();
        try {
            fis = new FileInputStream(entryFile);
            md5Str = DigestUtils.md5Hex(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis);
            System.gc();
        }
        return md5Str;
    }
}
