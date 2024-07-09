package top.linrty.netdisk.transfer.service;

import top.linrty.netdisk.common.domain.dto.transfer.DownloadFileDTO;
import top.linrty.netdisk.common.domain.dto.transfer.PreviewDTO;
import top.linrty.netdisk.common.domain.dto.transfer.UploadFileChunkDTO;
import top.linrty.netdisk.common.domain.vo.transfer.UploadFileVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IFileTransferService {
    UploadFileVO uploadFileSpeed(UploadFileChunkDTO uploadFileChunkDTO);

    void uploadFile(HttpServletRequest request, UploadFileChunkDTO uploadFileChunkDto, String userId);

    void downloadFile(HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO);

    void downloadUserFileList(HttpServletResponse httpServletResponse, String filePath, String fileName, List<String> userFileIds);

    void previewFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO);

    void previewPictureFile(HttpServletResponse httpServletResponse, PreviewDTO previewDTO);

    Boolean deleteFile(String fileId);

    Boolean deleteFileToRecovery(String fileId);

    String copyFile(String Url, String targetFilePath, String extendName);

    void copyFile(String filePath, String[] userFileIds);


    String downloadFile2Temp(String fileId);


    Map<String, Object> uploadTempFile2Netdisk(String fileUrl, String destUrl);

    List<String> unzipFile(String unzipFileTempUrl, String extendName);

    String getMd5(String fileUrl);
}
