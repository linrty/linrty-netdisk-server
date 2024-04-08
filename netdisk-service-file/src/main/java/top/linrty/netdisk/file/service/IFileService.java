package top.linrty.netdisk.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.file.domain.po.FileBean;
import top.linrty.netdisk.file.domain.po.Image;
import top.linrty.netdisk.file.domain.po.PictureFile;
import top.linrty.netdisk.file.domain.vo.FileDetailVO;

import java.util.List;

public interface IFileService extends IService<FileBean> {

    Long getFilePointCount(String fileId);

    void unzipFile(String userFileId, int unzipMode, String filePath);

    void updateFileDetail(String userFileId, String identifier, long fileSize);

    FileDetailVO getFileDetail(String userFileId);

    Boolean addFilePoint(String identifier, NetdiskFile netdiskFile);


    String addFile(String Url, Long fileSize, Integer storageType, String identifier);


    Boolean addImage(Image image);


    FileBean getFileByFileId(String fileId);


    PictureFile getPictureFileByFileUrl(String fileUrl);

    void newFile(String filePath, String fileName, String extendName);


    List<FileBean> getFileByIdentifier(String identifier);


}
