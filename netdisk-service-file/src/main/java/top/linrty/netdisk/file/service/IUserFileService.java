package top.linrty.netdisk.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.file.domain.po.TreeNode;
import top.linrty.netdisk.file.domain.po.UserFile;
import top.linrty.netdisk.common.domain.vo.file.FileListVO;

import java.util.List;

public interface IUserFileService extends IService<UserFile> {
    IPage<FileListVO> userFileList(String userId, String filePath, Long currentPage, Long pageCount);

    List<UserFile> selectUserFileByNameAndPath(String fileName, String filePath);

    List<UserFile> selectSameUserFile(String fileName, String filePath, String extendName);

    IPage<FileListVO> getFileByFileType(Integer fileTypeId, Long currentPage, Long pageCount, String userId);


    String addFile(UserFile userFile, String identifier);

    UserFile getUserFileInfo(String userFileId);


    List<String> getDirChildren(String dirPath, String userId);

    Boolean deleteUserFile( String userFileId);

    List<UserFile> selectUserFileByLikeRightFilePath(String filePath, String userId);

    void updateFileDeleteStateByFilePath(String filePath, String deleteBatchNum, String userId);

    Long getUserFilePointCount(String fileId);

    void userFileCopy(String userFileId, String newFilePath);

    List<UserFile> getUserFilePathTree();

    TreeNode getUserFileTree();

    void userFileMove(String userFileId, String newFilePath);

}
