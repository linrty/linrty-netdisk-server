package top.linrty.netdisk.file.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.common.constant.FileConstants;
import top.linrty.netdisk.common.constant.MQConstants;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.domain.po.UserFileInfo;
import top.linrty.netdisk.common.exception.DBException;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.RabbitMqHelper;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.po.FileBean;
import top.linrty.netdisk.file.domain.po.RecoveryFile;
import top.linrty.netdisk.file.domain.po.TreeNode;
import top.linrty.netdisk.file.domain.po.UserFile;
import top.linrty.netdisk.file.domain.vo.FileListVO;
import top.linrty.netdisk.file.mapper.FileMapper;
import top.linrty.netdisk.file.mapper.RecoveryFileMapper;
import top.linrty.netdisk.file.mapper.UserFileMapper;
import top.linrty.netdisk.file.service.IUserFileService;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserFileService extends ServiceImpl<UserFileMapper, UserFile> implements IUserFileService {

    @Resource
    UserFileMapper userFileMapper;


    @Resource
    FileMapper fileMapper;

    @Resource
    RecoveryFileMapper recoveryFileMapper;

    @Resource
    FileDealService fileDealService;

    @Resource
    RabbitMqHelper rabbitMqHelper;

    /**
     * 根据用户Id获取用户文件列表
     * @param userId
     * @param filePath
     * @param currentPage
     * @param pageCount
     * @return
     */
    @Override
    public IPage<FileListVO> userFileList(String userId, String filePath, Long currentPage, Long pageCount) {

        Page<FileListVO> page = new Page<>(currentPage, pageCount);
        UserFile userFile = new UserFile();

        if (userId == null) {
            userFile.setUserId(UserContext.getUser());
        } else {
            userFile.setUserId(userId);
        }

        userFile.setFilePath(URLDecoder.decodeForPath(filePath, StandardCharsets.UTF_8));

        return userFileMapper.selectPageVo(page, userFile, null);
    }


    /**
     * 根据文件类型获取文件列表
     * @param fileTypeId
     * @param currentPage
     * @param pageCount
     * @param userId
     * @return
     */
    @Override
    public IPage<FileListVO> getFileByFileType(Integer fileTypeId, Long currentPage, Long pageCount, String userId) {
        Page<FileListVO> page = new Page<>(currentPage, pageCount);

        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        return userFileMapper.selectPageVo(page, userFile, fileTypeId);
    }

    @Override
    public String addFile(UserFile userFile, String identifier) {
        try {
            userFileMapper.insert(userFile);
            rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL
                    , MQConstants.ROUTING_KEY_ADD_USER_FILE_TO_ES
                    , userFile.getUserFileId());
        } catch (Exception e) {
            // 出现异常
            UserFile userFile1 = userFileMapper.selectOne(new QueryWrapper<UserFile>().lambda()
                    .eq(UserFile::getUserId, userFile.getUserId())
                    .eq(UserFile::getFilePath, userFile.getFilePath())
                    .eq(UserFile::getFileName, userFile.getFileName())
                    .eq(UserFile::getExtendName, userFile.getExtendName())
                    .eq(UserFile::getDeleteFlag, userFile.getDeleteFlag())
                    .eq(UserFile::getIsDir, userFile.getIsDir()));
            FileBean file1 = fileMapper.selectById(userFile1.getFileId());
            if (!StrUtil.equals(identifier, file1.getIdentifier())) {
                // MD5码前后不一致
                log.warn("文件冲突重命名处理: {}", JSON.toJSONString(userFile1));
                String fileName = fileDealService.getRepeatFileName(userFile, userFile.getFilePath());
                userFile.setFileName(fileName);
                userFileMapper.insert(userFile);
                rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL
                        , MQConstants.ROUTING_KEY_ADD_USER_FILE_TO_ES
                        , userFile.getUserFileId());
                return userFile.getUserFileId();
            }
        }
        return userFile.getUserFileId();
    }

    @Override
    public UserFile getUserFileInfo(String userFileId) {
        return userFileMapper.selectById(userFileId);
    }

    @Override
    public List<String> getDirChildren(String dirPath, String userId) {
        List<UserFile> userFileList = userFileMapper.selectUserFileByLikeRightFilePath( dirPath, userId);
        return userFileList.stream().map(UserFile::getUserFileId).collect(Collectors.toList());
    }

    @Override
    public Boolean deleteUserFile(String userFileId) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        String uuid = UUID.randomUUID().toString();
        if (userFile.getIsDir() == 1) {
            LambdaUpdateWrapper<UserFile> userFileLambdaUpdateWrapper = new LambdaUpdateWrapper<UserFile>();
            userFileLambdaUpdateWrapper.set(UserFile::getDeleteFlag, RandomUtil.randomInt(FileConstants.deleteFileRandomSize))
                    .set(UserFile::getDeleteBatchNum, uuid)
                    .set(UserFile::getDeleteTime, DateUtil.now())
                    .eq(UserFile::getUserFileId, userFileId);
            userFileMapper.update(null, userFileLambdaUpdateWrapper);

            String filePath = new NetdiskFile(userFile.getFilePath(), userFile.getFileName(), true).getPath();
            // 加入到消息队列中
            Map<String, String> map = new HashMap<>();
            map.put("filePath", filePath);
            map.put("deleteBatchNum", uuid);
            map.put("userId", UserContext.getUser());
            rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_USER_FILE
                    , MQConstants.ROUTING_KEY_UPDATE_FILE_DELETE_STATE
                    , map);
            // updateFileDeleteStateByFilePath(filePath, uuid, UserContext.getUser());

        } else {
            LambdaUpdateWrapper<UserFile> userFileLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userFileLambdaUpdateWrapper.set(UserFile::getDeleteFlag, RandomUtil.randomInt(1, FileConstants.deleteFileRandomSize))
                    .set(UserFile::getDeleteTime, DateUtil.now())
                    .set(UserFile::getDeleteBatchNum, uuid)
                    .eq(UserFile::getUserFileId, userFileId);
            userFileMapper.update(null, userFileLambdaUpdateWrapper);
        }
        // 加入到回收站中
        RecoveryFile recoveryFile = new RecoveryFile();
        recoveryFile.setUserFileId(userFileId);
        recoveryFile.setDeleteTime(DateUtil.now());
        recoveryFile.setDeleteBatchNum(uuid);
        recoveryFileMapper.insert(recoveryFile);
        return true;
    }

    @Override
    public List<UserFile> selectUserFileByLikeRightFilePath(String filePath, String userId) {
        return userFileMapper.selectUserFileByLikeRightFilePath(filePath, userId);
    }


    @Override
    public void updateFileDeleteStateByFilePath(String filePath, String deleteBatchNum, String userId) {
        List<UserFile> fileList = selectUserFileByLikeRightFilePath(filePath, userId);
        List<String> userFileIds = fileList.stream().map(UserFile::getUserFileId).collect(Collectors.toList());

        //标记删除标志
        if (CollectionUtils.isNotEmpty(userFileIds)) {
            LambdaUpdateWrapper<UserFile> userFileLambdaUpdateWrapper1 = new LambdaUpdateWrapper<>();
            userFileLambdaUpdateWrapper1.set(UserFile::getDeleteFlag, RandomUtil.randomInt(FileConstants.deleteFileRandomSize))
                    .set(UserFile::getDeleteTime, DateUtil.now())
                    .set(UserFile::getDeleteBatchNum, deleteBatchNum)
                    .in(UserFile::getUserFileId, userFileIds)
                    .eq(UserFile::getDeleteFlag, 0);
            userFileMapper.update(null, userFileLambdaUpdateWrapper1);
        }
        for (String userFileId : userFileIds) {
            // 发送至消息队列进行处理
            rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL
                    , MQConstants.ROUTING_KEY_DELETE_USER_FILE_FROM_ES
                    , userFileId);
            // fileDealService.deleteESByUserFileId(userFileId);
        }
    }

    @Override
    public Long getUserFilePointCount(String fileId) {
        return userFileMapper.selectFilePointCount(fileId);
    }

    @Override
    public List<UserFile> selectSameUserFile(String fileName, String filePath, String extendName) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFileName, fileName)
                .eq(UserFile::getFilePath, filePath)
                .eq(UserFile::getUserId, UserContext.getUser())
                .eq(UserFile::getExtendName, extendName)
                .eq(UserFile::getDeleteFlag, "0");
        return userFileMapper.selectList(lambdaQueryWrapper);
    }


    @Override
    public List<UserFile> selectUserFileByNameAndPath(String fileName, String filePath) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFileName, fileName)
                .eq(UserFile::getFilePath, filePath)
                .eq(UserFile::getUserId, UserContext.getUser())
                .eq(UserFile::getDeleteFlag, 0);
        return userFileMapper.selectList(lambdaQueryWrapper);
    }

    /**
     *
     * 添加对应的用户文件至数据库
     * @param userFileId
     * @param newFilePath
     */
    @Override
    public void userFileCopy(String userFileId, String newFilePath) {
        // 根据userFileId先获取到对应的旧的文件信息
        UserFile userFile = userFileMapper.selectById(userFileId);
        String oldFilePath = userFile.getFilePath();
        String oldUserId = userFile.getUserId();
        String fileName = userFile.getFileName();

        // 生成新的文件信息
        userFile.setFilePath(newFilePath);
        userFile.setUserId(UserContext.getUser());
        userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());

        // 如果不是文件夹，需要判断是否有重名文件在同一级目录下
        if (userFile.getIsDir() == 0) {
            String repeatFileName = fileDealService.getRepeatFileName(userFile, userFile.getFilePath());
            userFile.setFileName(repeatFileName);
        }
        try {
            userFileMapper.insert(userFile);
        } catch (Exception e) {
            throw new DBException("文件复制失败,数据库插入失败");
        }

        oldFilePath = new NetdiskFile(oldFilePath, fileName, true).getPath();
        newFilePath = new NetdiskFile(newFilePath, fileName, true).getPath();

        // 如果是文件夹，需要递归复制文件夹下的文件
        if (userFile.isDirectory()) {
            List<UserFile> subUserFileList = userFileMapper.selectUserFileByLikeRightFilePath(oldFilePath, oldUserId);

            for (UserFile newUserFile : subUserFileList) {
                newUserFile.setFilePath(newUserFile.getFilePath().replaceFirst(oldFilePath, newFilePath));
                newUserFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
                if (newUserFile.isDirectory()) {
                    String repeatFileName = fileDealService.getRepeatFileName(newUserFile, newUserFile.getFilePath());
                    newUserFile.setFileName(repeatFileName);
                }
                newUserFile.setUserId(UserContext.getUser());
                try {
                    userFileMapper.insert(newUserFile);
                } catch (Exception e) {
                    throw new DBException("文件复制失败,数据库插入失败");
                }
            }
        }
    }

    @Override
    public List<UserFile> getUserFilePathTree() {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getUserId, UserContext.getUser())
                .eq(UserFile::getIsDir, 1)
                .eq(UserFile::getDeleteFlag, 0);
        return userFileMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public TreeNode getUserFileTree() {
        List<UserFile> userFileList = getUserFilePathTree();
        TreeNode resultTreeNode = new TreeNode();
        resultTreeNode.setLabel(NetdiskFile.separator);
        resultTreeNode.setId(0L);
        long id = 1;
        for (int i = 0; i < userFileList.size(); i++){
            UserFile userFile = userFileList.get(i);
            NetdiskFile netdiskFile = new NetdiskFile(userFile.getFilePath(), userFile.getFileName(), false);
            String filePath = netdiskFile.getPath();

            Queue<String> queue = new LinkedList<>();

            String[] strArr = filePath.split(NetdiskFile.separator);
            for (int j = 0; j < strArr.length; j++){
                if (!"".equals(strArr[j]) && strArr[j] != null){
                    queue.add(strArr[j]);
                }

            }
            if (queue.isEmpty()){
                continue;
            }

            resultTreeNode = fileDealService.insertTreeNode(resultTreeNode, id++, NetdiskFile.separator, queue);


        }
        List<TreeNode> treeNodeList = resultTreeNode.getChildren();
        treeNodeList.sort((o1, o2) -> {
            long i = o1.getId() - o2.getId();
            return (int) i;
        });
        return resultTreeNode;
    }


    @Override
    public void userFileMove(String userFileId, String newFilePath) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        String oldFilePath = userFile.getFilePath();
        String fileName = userFile.getFileName();

        if (userFile.isDirectory()){
            NetdiskFile netdiskFile = new NetdiskFile(oldFilePath, fileName, true);
            if (newFilePath.startsWith(netdiskFile.getPath() + NetdiskFile.separator)
                    || newFilePath.equals(netdiskFile.getPath())){
                throw new FileOperationException("不能移动到自己的子目录下");
            }
        }

        userFile.setFilePath(newFilePath);

        if (userFile.getIsDir() == 0){
            String repeatFileName = fileDealService.getRepeatFileName(userFile, userFile.getFilePath());
            userFile.setFileName(repeatFileName);
        }

        try {
            userFileMapper.updateById(userFile);
        } catch(Exception e){
            throw new DBException("文件移动失败,数据库修改失败");
        }

        oldFilePath = new NetdiskFile(oldFilePath, fileName, true).getPath();
        newFilePath = new NetdiskFile(newFilePath, fileName, true).getPath();

        if (userFile.isDirectory()){
            List<UserFile> subUserFileList = userFileMapper.selectUserFileByLikeRightFilePath(oldFilePath, UserContext.getUser());
            for (UserFile newUserFile : subUserFileList){
                newUserFile.setFilePath(newUserFile.getFilePath().replaceFirst(oldFilePath, newFilePath));
                if (newUserFile.getIsDir() == 0){
                    String repeatFileName = fileDealService.getRepeatFileName(newUserFile, newUserFile.getFilePath());
                    newUserFile.setFileName(repeatFileName);
                }
                try {
                    userFileMapper.updateById(newUserFile);
                } catch (Exception e){
                    throw new DBException("文件移动失败,数据库修改失败");
                }
            }
        }

    }
}
