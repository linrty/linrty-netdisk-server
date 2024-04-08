package top.linrty.netdisk.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.file.domain.po.RecoveryFile;
import top.linrty.netdisk.file.domain.vo.RecoveryFileListVO;

import java.util.List;

public interface IRecoveryFileService extends IService<RecoveryFile> {
    List<RecoveryFileListVO> selectRecoveryFileList();

    void restoreFile(String deleteBatchNum, String filePath);

    void deleteUserFileByDeleteBatchNum(String deleteBatchNum);

    void deleteRecoveryFile(String userFileId);

    void batchDeleteRecoveryFile(String userFileIds);

    void deleteRecoveryFileTask(String userFileId);
}

