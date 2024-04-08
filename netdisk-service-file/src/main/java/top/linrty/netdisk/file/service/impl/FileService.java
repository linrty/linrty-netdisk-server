package top.linrty.netdisk.file.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
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
import top.linrty.netdisk.file.domain.po.*;
import top.linrty.netdisk.file.domain.vo.FileDetailVO;
import top.linrty.netdisk.file.mapper.*;
import top.linrty.netdisk.file.service.IFileService;
import top.linrty.netdisk.file.service.IUserFileService;
import top.linrty.netdisk.file.util.NetdiskFileUtil;

import javax.annotation.Resource;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class FileService extends ServiceImpl<FileMapper, FileBean> implements IFileService {
    public static Executor executor = Executors.newFixedThreadPool(20);
    @Resource
    FileMapper fileMapper;
    @Resource
    UserFileMapper userFileMapper;

//    @Value("${ufop.storage-type}")
//    private Integer storageType;

//    @Resource
//    AsyncTaskComp asyncTaskComp;
    @Resource
    MusicMapper musicMapper;
    @Resource
    ImageMapper imageMapper;

    @Resource
    PictureFileMapper pictureFileMapper;


    @Resource
    FileDealService fileDealService;


    @Resource
    IUserFileService userFileService;

    @Resource
    TransferClient transferClient;

    @Resource
    RabbitMqHelper rabbitMqHelper;


    /**
     * 添加文件指针
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addFilePoint(String identifier, NetdiskFile netdiskFile) {
        Map<String, Object> param = new HashMap<>();
        param.put("identifier", identifier);
        List<FileBean> list = fileMapper.selectByMap(param);
        if (list != null && !list.isEmpty()) {
            FileBean file = list.get(0);
            UserFile userFile = new UserFile(netdiskFile, UserContext.getUser(), file.getFileId());
            try {
                userFileMapper.insert(userFile);
                rabbitMqHelper.sendMessage(
                        MQConstants.EXCHANGE_FILE_DEAL,
                        MQConstants.ROUTING_KEY_ADD_USER_FILE_TO_ES,
                        userFile.getUserFileId()
                );
                // fileDealService.uploadESByUserFileId(userFile.getUserFileId());
            } catch (Exception e) {
                log.warn("极速上传文件冲突重命名处理: {}", JSON.toJSONString(userFile));

            }
            return true;
        }
        // 未找到对应的文件，添加指针失败
        return false;
    }




    /**
     * 获取文件指向数
     * @param fileId
     * @return
     */
    @Override
    public Long getFilePointCount(String fileId) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFileId, fileId);
        long count = userFileMapper.selectCount(lambdaQueryWrapper);
        return count;
    }


    /**
     * 解压文件
     * @param unzipUserFileId 待解压的userFileId
     * @param unzipMode  解压模式
     * @param unzipTargetFilePath  解压到的目标路径
     */
    @Override
    @GlobalTransactional
    public void unzipFile(String unzipUserFileId, int unzipMode, String unzipTargetFilePath) {
        // 获取待解压的文件信息
        UserFile unzipUserFile = userFileMapper.selectById(unzipUserFileId);

        // 将文件下载到缓存区
        String unzipFileTempUrl = transferClient.downloadFile2Temp(unzipUserFile.getFileId());

        // 获取解压目录Url
        String unzipDirTempUrl = unzipFileTempUrl.replace("." + unzipUserFile.getExtendName(), "");

        // 解压文件至同一级目录的同名文件夹下,返回所有子文件名
        List<String> fileEntryNameList = transferClient.unzipFile(unzipFileTempUrl, unzipUserFile.getExtendName());

        // 删除缓存区的压缩文件
        transferClient.deleteTempFile(unzipFileTempUrl);

        // 如果解压成功，且解压模式为1，表示需要在用户网盘的当前目录新建一个文件夹
        if (!fileEntryNameList.isEmpty() && unzipMode == 1) {
            UserFile unzipDir = NetdiskFileUtil.getNetdiskDir(unzipUserFile.getUserId(), unzipUserFile.getFilePath(), unzipUserFile.getFileName());
            userFileMapper.insert(unzipDir);
        }

        // 将所有解压出的文件从缓存区中上传到对应网盘区中，并添加对应的文件指针和文件信息（异步完成）
        for(String entryFileName: fileEntryNameList){
            Map<String, String>map = new HashMap<>();
            map.put("unzipDirTempUrl", unzipDirTempUrl);
            map.put("entryFileName", entryFileName);
            map.put("userId", UserContext.getUser());
            if (unzipMode == 0) { // 解压到当前目录
                map.put("savePath", unzipUserFile.getFilePath());
            } else if (unzipMode == 1) {  // 解压到同级的同名目录下
                map.put("savePath", unzipUserFile.getFilePath() + "/" + unzipUserFile.getFileName());
            } else if (unzipMode == 2) {  // 解压到指定目录
                map.put("savePath", unzipTargetFilePath);
            }
            // 发送保存子文件的消息
            rabbitMqHelper.sendMessage(
                    MQConstants.EXCHANGE_FILE_DEAL,
                    MQConstants.ROUTING_KEY_SAVE_UNZIP_FILE,
                    map
            );
        }
    }

    /**
     * 更新文件详情
     * @param userFileId
     * @param identifier
     * @param fileSize
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileDetail(String userFileId, String identifier, long fileSize) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        String currentTime = DateUtil.now();
        FileBean fileBean = new FileBean();
        fileBean.setIdentifier(identifier);
        fileBean.setFileSize(fileSize);
        fileBean.setModifyTime(currentTime);
        fileBean.setModifyUserId(UserContext.getUser());
        fileBean.setFileId(userFile.getFileId());
        fileMapper.updateById(fileBean);
        userFile.setUploadTime(currentTime);
        userFile.setModifyTime(currentTime);
        userFile.setModifyUserId(UserContext.getUser());
        userFileMapper.updateById(userFile);
    }

    /**
     * 获取文件详情
     * @param userFileId
     * @return
     */
    @Override
    public FileDetailVO getFileDetail(String userFileId) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        FileBean fileBean = fileMapper.selectById(userFile.getFileId());
        Music music = musicMapper.selectOne(new QueryWrapper<Music>().eq("fileId", userFile.getFileId()));
        Image image = imageMapper.selectOne(new QueryWrapper<Image>().eq("fileId", userFile.getFileId()));

        if ("mp3".equalsIgnoreCase(userFile.getExtendName()) || "flac".equalsIgnoreCase(userFile.getExtendName())) {
            if (music == null) {
                fileDealService.parseMusicFile(userFile.getExtendName(), fileBean.getStorageType(), fileBean.getFileUrl(), fileBean.getFileId());
                music = musicMapper.selectOne(new QueryWrapper<Music>().eq("fileId", userFile.getFileId()));
            }
        }

        FileDetailVO fileDetailVO = new FileDetailVO();
        BeanUtil.copyProperties(userFile, fileDetailVO);
        BeanUtil.copyProperties(fileBean, fileDetailVO);
        fileDetailVO.setMusic(music);
        fileDetailVO.setImage(image);
        return fileDetailVO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addFile(String Url, Long fileSize, Integer storageType, String identifier) {
        FileBean fileBean = FileBean.builder()
                                .fileId(IdUtil.getSnowflakeNextIdStr())
                                .fileUrl(Url)
                                .fileSize(fileSize)
                                .fileStatus(1)
                                .storageType(storageType)
                                .identifier(identifier)
                                .createTime(DateUtil.now())
                                .createUserId(UserContext.getUser())
                                .build();
        try {
            fileMapper.insert(fileBean);
        } catch (Exception e) {
            log.warn("identifier Duplicate: {}", fileBean.getIdentifier());
            fileBean = fileMapper.selectOne(new QueryWrapper<FileBean>().lambda().eq(FileBean::getIdentifier, fileBean.getIdentifier()));
        }
        return fileBean.getFileId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addImage(Image image) {
        try{
            imageMapper.insert(image);
        } catch (Exception e) {
            log.warn("生成缩略图失败: {}", image.getFileId());
            return false;
        }
        return true;
    }

    @Override
    public FileBean getFileByFileId(String fileId) {
        return fileMapper.selectById(fileId);
    }


    @Override
    public PictureFile getPictureFileByFileUrl(String fileUrl) {
        byte[] bytesUrl = Base64.getDecoder().decode(fileUrl);
        PictureFile pictureFile = new PictureFile();
        pictureFile.setFileUrl(new String(bytesUrl));
        return pictureFileMapper.selectOne(new QueryWrapper<>(pictureFile));
    }

    @Override
    @GlobalTransactional
    public void newFile(String filePath, String fileName, String extendName) {
        List<UserFile> userFiles = userFileService.selectSameUserFile(fileName, filePath, extendName);
        if (userFiles != null && !userFiles.isEmpty()) {
            throw new FileOperationException("同名文件已存在");
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        // 模板文件地址
        String templateFilePath = "";
        if ("docx".equals(extendName)) {
            templateFilePath = "template/Word.docx";
        } else if ("xlsx".equals(extendName)) {
            templateFilePath = "template/Excel.xlsx";
        } else if ("pptx".equals(extendName)) {
            templateFilePath = "template/PowerPoint.pptx";
        } else if ("txt".equals(extendName)) {
            templateFilePath = "template/Text.txt";
        } else if ("drawio".equals(extendName)) {
            templateFilePath = "template/Drawio.drawio";
        }

        // 远程调用copy模板文件
        String fileUrl = transferClient.copyFile("static/" + templateFilePath, filePath, extendName);

        String storageType = transferClient.getStorageType();

        FileBean fileBean = new FileBean();
        fileBean.setFileId(IdUtil.getSnowflakeNextIdStr());
        fileBean.setFileSize(0L);
        fileBean.setFileUrl(fileUrl);
        fileBean.setStorageType(Integer.parseInt(storageType));
        fileBean.setIdentifier(uuid);
        fileBean.setCreateTime(DateUtil.now());
        fileBean.setCreateUserId(UserContext.getUser());
        fileBean.setFileStatus(1);
        boolean saveFlag = save(fileBean);
        UserFile userFile = new UserFile();

        // 添加用户指针指向该文件加入数据库中
        if (saveFlag) {
            userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
            userFile.setUserId(UserContext.getUser());
            userFile.setFileName(fileName);
            userFile.setFilePath(filePath);
            userFile.setDeleteFlag(0);
            userFile.setIsDir(0);
            userFile.setExtendName(extendName);
            userFile.setUploadTime(DateUtil.now());
            userFile.setFileId(fileBean.getFileId());
            userFile.setCreateTime(DateUtil.now());
            userFile.setCreateUserId(UserContext.getUser());
            userFileService.save(userFile);
        }
    }

    @Override
    public List<FileBean> getFileByIdentifier(String identifier) {
        Map<String, Object> param = new HashMap<>();
        param.put("identifier", identifier);
        return fileMapper.selectByMap(param);
    }
}
