package top.linrty.netdisk.transfer.controller;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import top.linrty.netdisk.common.anno.MyLog;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.domain.dto.transfer.*;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.exception.UserException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.common.util.MimeUtils;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.transfer.api.FileClient;
import top.linrty.netdisk.transfer.domain.po.Range;
import top.linrty.netdisk.transfer.domain.po.operation.download.Downloader;
import top.linrty.netdisk.transfer.domain.po.operation.download.entity.DownloadFile;
import top.linrty.netdisk.common.domain.vo.transfer.UploadFileVO;
import top.linrty.netdisk.transfer.service.IFileTransferService;
import top.linrty.netdisk.transfer.util.FileStorageFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
// @Tag(name = "filetransfer", description = "该接口为文件传输接口，主要用来做文件的上传、下载和预览")
@RestController
@RequestMapping("/filetransfer")
public class FileTransferController {

    @Resource
    IFileTransferService fileTransferService;

    @Resource
    FileClient fileClient;

    @Resource
    FileStorageFactory fileStorageFactory;


    public static final String CURRENT_MODULE = "文件传输接口";

    // @Operation(summary = "极速上传", description = "校验文件MD5判断文件是否存在，如果存在直接上传成功并返回skipUpload=true，如果不存在返回skipUpload=false需要再次调用该接口的POST方法", tags = {"filetransfer"})
    @RequestMapping(value = "/uploadfile", method = RequestMethod.GET)
    @MyLog(operation = "极速上传", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<UploadFileVO> uploadFileSpeed(UploadFileChunkDTO uploadFileChunkDTO) {
        UploadFileVO uploadFileVO = fileTransferService.uploadFileSpeed(uploadFileChunkDTO);
        return RestResult.success().data(uploadFileVO);

    }

    // @Operation(summary = "上传文件", description = "真正的上传文件接口", tags = {"filetransfer"})
    @RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
    @MyLog(operation = "上传文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<UploadFileVO> uploadFile(HttpServletRequest request, UploadFileChunkDTO uploadFileChunkDTO) {

        fileTransferService.uploadFile(request, uploadFileChunkDTO, UserContext.getUser());

        UploadFileVO uploadFileVO = new UploadFileVO();
        return RestResult.success().data(uploadFileVO);

    }


    // @Operation(summary = "下载文件", description = "下载文件接口", tags = {"filetransfer"})
    @MyLog(operation = "下载文件", module = CURRENT_MODULE)
    @RequestMapping(value = "/downloadfile", method = RequestMethod.GET)
    public void downloadFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO) {
        boolean authResult = fileClient.checkAuthDownloadPreview(downloadFileDTO.getShareBatchNum(),
                downloadFileDTO.getExtractionCode(),
                downloadFileDTO.getUserFileId(), null);
        if (!authResult) {
            log.error("没有权限下载！！！");
            throw new UserException("没有权限下载！！！");
        }
        httpServletResponse.setContentType("application/force-download");// 设置强制下载不打开
        Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(downloadFileDTO.getUserFileId());
        String fileName = "";
        if (userFileInfo.get("isDir").equals(1)) {
            fileName = userFileInfo.get("fileName") + ".zip";
        } else {
            fileName = userFileInfo.get("fileName") + "." + userFileInfo.get("extendName");

        }
        try {
            fileName = new String(fileName.getBytes("utf-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名

        fileTransferService.downloadFile(httpServletResponse, downloadFileDTO);
    }


    /**
     * 删除文件并加入到回收站
     * @param deleteFileDto
     * @return
     */
    //@Operation(summary = "删除文件", description = "可以删除文件或者目录", tags = {"file"})
    @RequestMapping(value = "/deletefile", method = RequestMethod.POST)
    @MyLog(operation = "删除文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult deleteFile(@RequestBody DeleteFileDTO deleteFileDto) {
        if (fileTransferService.deleteFileToRecovery(deleteFileDto.getUserFileId())) {
            return RestResult.success();
        }
        return RestResult.fail().message("删除失败");
    }

    /**
     * 删除文件本体
     * @param fileId
     * @return
     */
    @RequestMapping(value = "/delete_file", method = RequestMethod.GET)
    @MyLog(operation = "删除文件", module = CURRENT_MODULE)
    @ResponseBody
    public Boolean deleteFile(String fileId) {
        return fileTransferService.deleteFile(fileId);
    }



    // @Operation(summary = "批量下载文件", description = "批量下载文件", tags = {"filetransfer"})
    @RequestMapping(value = "/batchDownloadFile", method = RequestMethod.GET)
    @MyLog(operation = "批量下载文件", module = CURRENT_MODULE)
    @ResponseBody
    public void batchDownloadFile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BatchDownloadFileDTO batchDownloadFileDTO) {

        boolean authResult = fileClient.checkAuthDownloadPreview(batchDownloadFileDTO.getShareBatchNum(),
                batchDownloadFileDTO.getExtractionCode(),
                batchDownloadFileDTO.getUserFileIds(), null);
        if (!authResult) {
            log.error("没有权限下载！！！");
            return;
        }


        String files = batchDownloadFileDTO.getUserFileIds();
        String[] userFileIdStrs = files.split(",");
        List<String> userFileIds = new ArrayList<>();
        for(String userFileId : userFileIdStrs) {
            Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(userFileId);

            if (userFileInfo.get("isDir").equals(0)) {
                userFileIds.add(userFileId);
            } else {
                NetdiskFile netdiskFile = new NetdiskFile(
                        (String) userFileInfo.get("filePath"),
                        (String) userFileInfo.get("fileName"),
                        true);

                List<String> childrenFileIds = fileClient.getDirChildren(netdiskFile.getPath(), (String) userFileInfo.get("userId"));
                userFileIds.add((String) userFileInfo.get("userFileId"));
                userFileIds.addAll(childrenFileIds);
            }
        }
        Map<String, Object> userFileInfoMap = fileClient.getUserFileInfoMap(userFileIdStrs[0]);
        httpServletResponse.setContentType("application/force-download");// 设置强制下载不打开
        Date date = new Date();
        String fileName = String.valueOf(date.getTime());
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + fileName + ".zip");// 设置文件名
        fileTransferService.downloadUserFileList(httpServletResponse,(String) userFileInfoMap.get("filePath"), fileName, userFileIds);
    }

    // @Operation(summary="预览文件", description="用于文件预览", tags = {"filetransfer"})
    @GetMapping("/preview")
    public void preview(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,  PreviewDTO previewDTO) throws IOException {

        if (previewDTO.getPlatform() != null && previewDTO.getPlatform() == 2) {
            fileTransferService.previewPictureFile(httpServletResponse, previewDTO);
            return ;
        }


//        boolean authResult = fileClient.checkAuthDownloadPreview(
//                previewDTO.getShareBatchNum(),
//                previewDTO.getExtractionCode(),
//                previewDTO.getUserFileId(),
//                previewDTO.getPlatform()
//        );
//        if (!authResult) {
//            log.error("没有权限预览！！！");
//            return;
//        }


        Map<String, Object> userFileInfo = fileClient.getUserFileInfoMap(previewDTO.getUserFileId());
        String fileName = (String) userFileInfo.get("fileName") + "." + (String) userFileInfo.get("extendName");
        try {
            fileName = new String(fileName.getBytes("utf-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new FileOperationException("文件名编码错误");
        }

        httpServletResponse.addHeader("Content-Disposition", "fileName=" + fileName);// 设置文件名
        String mime = MimeUtils.getMime((String) userFileInfo.get("extendName"));
        httpServletResponse.setHeader("Content-Type", mime);
        if (FileTypeUtil.isImageFile((String) userFileInfo.get("extendName"))) {
            httpServletResponse.setHeader("cache-control", "public");
        }

        Map<String, Object> fileInfo = fileClient.getFileInfoMap((String) userFileInfo.get("fileId"));

        if (FileTypeUtil.isVideoFile((String) userFileInfo.get("extendName"))
                || "mp3".equalsIgnoreCase((String) userFileInfo.get("extendName"))
                || "flac".equalsIgnoreCase((String) userFileInfo.get("extendName"))) {
            //获取从那个字节开始读取文件
            String rangeString = httpServletRequest.getHeader("Range");
            int start = 0;
            if (StrUtil.isNotBlank(rangeString)) {
                start = Integer.parseInt(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
            }

            Downloader downloader = fileStorageFactory.getDownloader((Integer) fileInfo.get("storageType"));
            DownloadFile downloadFile = new DownloadFile();
            downloadFile.setFileUrl((String) fileInfo.get("fileUrl"));
            Range range = new Range();
            range.setStart(start);

            if (start + 1024 * 1024 * 1 >= (Integer)(fileInfo.get("fileSize"))) {
                range.setLength((Integer)(fileInfo.get("fileSize")) - start);
            } else {
                range.setLength(1024 * 1024 * 1);
            }
            downloadFile.setRange(range);
            InputStream inputStream = downloader.getInputStream(downloadFile);

            OutputStream outputStream = httpServletResponse.getOutputStream();
            try {

                //返回码需要为206，代表只处理了部分请求，响应了部分数据

                httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                // 每次请求只返回1MB的视频流

                httpServletResponse.setHeader("Accept-Ranges", "bytes");
                //设置此次相应返回的数据范围
                httpServletResponse.setHeader("Content-Range", "bytes "
                        + start
                        + "-"
                        + ((Integer)(fileInfo.get("fileSize")) - 1)
                        + "/"
                        + (Integer)(fileInfo.get("fileSize")));
                IOUtils.copy(inputStream, outputStream);


            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
                if (downloadFile.getOssClient() != null) {
                    downloadFile.getOssClient().shutdown();
                }
            }

        } else {
            fileTransferService.previewFile(httpServletResponse, previewDTO);
        }

    }

    // @Operation(summary = "文件复制", description = "可以复制文件或者目录", tags = {"file"})
    @RequestMapping(value = "/copyfile", method = RequestMethod.POST)
    @MyLog(operation = "文件复制", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> copyFile(@RequestBody CopyFileDTO copyFileDTO) {
        String filePath = copyFileDTO.getFilePath();
        String userFileIds = copyFileDTO.getUserFileIds();
        String[] userFileIdArr = userFileIds.split(",");
        fileTransferService.copyFile(filePath, userFileIdArr);
        return RestResult.success();

    }

    @RequestMapping(value = "/copy_file", method = RequestMethod.GET)
    @MyLog(operation = "复制文件", module = CURRENT_MODULE)
    @ResponseBody
    public String copyFile(String Url, String targetFilePath, String extendName) {
        return fileTransferService.copyFile(Url, targetFilePath, extendName);
    }

    @RequestMapping(value = "get_storage_type", method = RequestMethod.GET)
    @ResponseBody
    public String getStorageType() {
        return fileStorageFactory.getStorageType();
    }


    @RequestMapping(value = "/download_file_to_temp", method = RequestMethod.GET)
    @ResponseBody
    public String downloadFile2Temp(@RequestParam String fileId) {
        return fileTransferService.downloadFile2Temp(fileId);
    }

    @RequestMapping(value = "/upload_temp_file_to_netdisk", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> uploadTempFile2Netdisk(@RequestParam String fileUrl, @RequestParam String extendName) {
        String md5 = fileTransferService.getMd5(fileUrl);
        System.out.println("md5: " + md5);
        String saveFileUrl = FileTypeUtil.getStaticPath()+ File.separator + FileTypeUtil.getUploadFileUrl(md5, extendName);
        System.out.println("saveFileUrl: " + saveFileUrl);
        System.out.println("fileUrl: " + fileUrl);
        Map<String, Object>map = fileTransferService.uploadTempFile2Netdisk(FileTypeUtil.formatPath(fileUrl), saveFileUrl);
        map.put("fileUrl", FileTypeUtil.getUploadFileUrl(md5, extendName));
        return map;
    }

    @RequestMapping(value = "/unzip_file", method = RequestMethod.GET)
    @ResponseBody
    public List<String> unzipFile(@RequestParam String unzipFileTempUrl, @RequestParam String extendName) {
        return fileTransferService.unzipFile(unzipFileTempUrl, extendName);
    }

    @RequestMapping(value = "/delete_temp_file", method = RequestMethod.GET)
    @ResponseBody
    public void deleteTempFile(@RequestParam String tempFileUrl){
        File file = new File(tempFileUrl);
        if (file.exists()) {
            file.delete();
        }
    }

    @RequestMapping(value = "/is_dir_temp_file", method = RequestMethod.GET)
    @ResponseBody
    public Boolean isDirTempFile(@RequestParam String tempFileUrl){
        File file = new File(tempFileUrl);
        return file.isDirectory();
    }


    @RequestMapping(value = "/get_md5", method = RequestMethod.GET)
    public String getMd5(@RequestParam String fileUrl){
        return fileTransferService.getMd5(fileUrl);
    }
}
