package top.linrty.netdisk.file.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.common.constant.MQConstants;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.RabbitMqHelper;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.api.TransferClient;
import top.linrty.netdisk.file.domain.po.FileBean;
import top.linrty.netdisk.file.domain.po.RecoveryFile;
import top.linrty.netdisk.file.domain.po.UserFile;
import top.linrty.netdisk.common.domain.vo.file.RecoveryFileListVO;
import top.linrty.netdisk.file.mapper.FileMapper;
import top.linrty.netdisk.file.mapper.RecoveryFileMapper;
import top.linrty.netdisk.file.mapper.UserFileMapper;
import top.linrty.netdisk.file.service.IRecoveryFileService;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class RecoveryFileService  extends ServiceImpl<RecoveryFileMapper, RecoveryFile> implements IRecoveryFileService {

    @Resource
    RecoveryFileMapper recoveryFileMapper;

    @Resource
    UserFileMapper userFileMapper;

    @Resource
    FileMapper fileMapper;

    @Resource
     FileDealService fileDealService;

    @Resource
    TransferClient transferClient;

    @Resource
    RabbitMqHelper rabbitMqHelper;

    @Override
    public List<RecoveryFileListVO> selectRecoveryFileList() {
        return recoveryFileMapper.selectRecoveryFileList(UserContext.getUser());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFile(String deleteBatchNum, String filePath) {
        // 通过删除批次号查询出所有删除的文件
        List<UserFile> restoreUserFileList = userFileMapper.selectList(new QueryWrapper<UserFile>().lambda().eq(UserFile::getDeleteBatchNum, deleteBatchNum));
        // 还原文件
        for (UserFile restoreUserFile : restoreUserFileList) {
            // 设置删除标记为0
            restoreUserFile.setDeleteFlag(0);
            restoreUserFile.setDeleteBatchNum(deleteBatchNum);
            // 重命名文件名
            String fileName = fileDealService.getRepeatFileName(restoreUserFile, restoreUserFile.getFilePath());
            // 判断是否是文件夹
            if (restoreUserFile.isDirectory()) {
                // 如果是文件夹，判断文件夹名是否有变化
                if (!StrUtil.equals(fileName, restoreUserFile.getFileName())) {
                    // 如果文件夹名有变化，更新文件夹名
                    userFileMapper.deleteById(restoreUserFile);
                } else {
                    // 如果没变化直接更新
                    userFileMapper.updateById(restoreUserFile);
                }
            } else if (restoreUserFile.isFile()) {
                // 如果是文件
                restoreUserFile.setFileName(fileName);
                userFileMapper.updateById(restoreUserFile);
            }
        }
        // 还原文件夹
        NetdiskFile netdiskFile = new NetdiskFile(filePath, true);
        fileDealService.restoreParentFilePath(netdiskFile, UserContext.getUser());

        // 删除回收文件
        LambdaQueryWrapper<RecoveryFile> recoveryFileServiceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        recoveryFileServiceLambdaQueryWrapper.eq(RecoveryFile::getDeleteBatchNum, deleteBatchNum);
        recoveryFileMapper.delete(recoveryFileServiceLambdaQueryWrapper);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserFileByDeleteBatchNum(String deleteBatchNum) {
        LambdaQueryWrapper<UserFile> userFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFileLambdaQueryWrapper.eq(UserFile::getDeleteBatchNum, deleteBatchNum);
        userFileMapper.delete(userFileLambdaQueryWrapper);
    }


    @Override
    public void deleteRecoveryFile(String userFileId) {
        RecoveryFile recoveryFile = recoveryFileMapper.selectOne(new QueryWrapper<RecoveryFile>().lambda().eq(RecoveryFile::getUserFileId, userFileId));
        if (recoveryFile == null) {
            throw new FileOperationException("回收文件不存在");
        }
        // 发送消息处理彻底删除文件
        rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_RECOVERY_FILE
                , MQConstants.ROUTING_KEY_DELETE_FILE
                , userFileId);
        recoveryFileMapper.deleteById(recoveryFile.getRecoveryFileId());
    }


    @Override
    public void batchDeleteRecoveryFile(String userFileIds) {
        String[] userFileIdList = userFileIds.split(",");
        for (String userFileId : userFileIdList) {
            RecoveryFile recoveryFile = recoveryFileMapper.selectOne(new QueryWrapper<RecoveryFile>().lambda().eq(RecoveryFile::getUserFileId, userFileId));
            if (recoveryFile == null) {
                throw new FileOperationException("回收文件不存在");
            }
            // 发送消息处理彻底删除文件
            rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_RECOVERY_FILE
                    , MQConstants.ROUTING_KEY_DELETE_FILE
                    , userFileId);
            recoveryFileMapper.deleteById(recoveryFile.getRecoveryFileId());
        }
    }

    @Override
    @GlobalTransactional
    public void deleteRecoveryFileTask(String userFileId) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        if (userFile.getIsDir() == 1) {
            // 文件夹类型的删除
            LambdaQueryWrapper<UserFile> userFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userFileLambdaQueryWrapper.eq(UserFile::getDeleteBatchNum, userFile.getDeleteBatchNum());
            List<UserFile> list = userFileMapper.selectList(userFileLambdaQueryWrapper);
            deleteUserFileByDeleteBatchNum(userFile.getDeleteBatchNum());
            for (UserFile userFileItem : list) {

                Long filePointCount = userFileMapper.selectFilePointCount(userFileItem.getFileId());

                if (filePointCount != null && filePointCount == 0 && userFileItem.getIsDir() == 0) {
                    FileBean fileBean = fileMapper.selectById(userFileItem.getFileId());
                    if (fileBean != null) {
                        try {
                            // 删除文件本体
                            if (transferClient.deleteFile(fileBean.getFileId())){
                                fileMapper.deleteById(fileBean.getFileId());
                            }else{
                                throw new FileOperationException("删除文件失败");
                            }
                            // filetransferService.deleteFile(fileBean);
                        } catch (Exception e) {
                            log.error("删除本地文件失败：" + JSON.toJSONString(fileBean));
                        }
                    }


                }
            }
        } else {

            deleteUserFileByDeleteBatchNum(userFile.getDeleteBatchNum());
            Long filePointCount = userFileMapper.selectFilePointCount(userFile.getFileId());

            if (filePointCount != null && filePointCount == 0 && userFile.getIsDir() == 0) {
                FileBean fileBean = fileMapper.selectById(userFile.getFileId());
                try {
                    if (transferClient.deleteFile(fileBean.getFileId())){
                        fileMapper.deleteById(fileBean.getFileId());
                    }else{
                        throw new FileOperationException("删除文件失败");
                    }
                } catch (Exception e) {
                    log.error("删除本地文件失败：" + JSON.toJSONString(fileBean));
                }
            }
        }
    }


}
