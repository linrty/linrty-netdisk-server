package top.linrty.netdisk.file.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.common.constant.MQConstants;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.FileTypeUtil;
import top.linrty.netdisk.common.util.RabbitMqHelper;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.api.TransferClient;
import top.linrty.netdisk.file.domain.po.*;
import top.linrty.netdisk.file.mapper.FileMapper;
import top.linrty.netdisk.file.mapper.MusicMapper;
import top.linrty.netdisk.file.mapper.ShareMapper;
import top.linrty.netdisk.file.mapper.UserFileMapper;
import top.linrty.netdisk.file.service.IShareFileService;
import top.linrty.netdisk.file.service.IShareService;
import top.linrty.netdisk.file.util.NetdiskFileUtil;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@Slf4j
public class FileDealService {
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private FileMapper fileMapper;
//    @Resource
//    private IUserService userService;
    // @Resource
    // private IShareService shareService;
    @Resource
    ShareMapper shareMapper;

    @Resource
    private IShareFileService shareFileService;
    @Resource
    private MusicMapper musicMapper;
    // Elasticsearch功能

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private RabbitMqHelper rabbitMqHelper;

    @Resource
    private TransferClient transferClient;

    /**
     * 获取重复文件名
     * <p>
     * 场景1: 文件还原时，在 savefilePath 路径下，保存 测试.txt 文件重名，则会生成 测试(1).txt
     * 场景2： 上传文件时，在 savefilePath 路径下，保存 测试.txt 文件重名，则会生成 测试(1).txt
     *
     * @param userFile
     * @param savefilePath
     * @return
     */
    public String getRepeatFileName(UserFile userFile, String savefilePath) {
        String fileName = userFile.getFileName();
        String extendName = userFile.getExtendName();

        String userId = userFile.getUserId();
        int isDir = userFile.getIsDir();
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFilePath, savefilePath)
                .eq(UserFile::getDeleteFlag, 0)
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getFileName, fileName)
                .eq(UserFile::getIsDir, isDir);
        if (userFile.isFile()) {
            lambdaQueryWrapper.eq(UserFile::getExtendName, extendName);
        }
        List<UserFile> list = userFileMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return fileName;
        }

        int i = 0;

        while (!CollectionUtils.isEmpty(list)) {
            i++;
            LambdaQueryWrapper<UserFile> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(UserFile::getFilePath, savefilePath)
                    .eq(UserFile::getDeleteFlag, 0)
                    .eq(UserFile::getUserId, userId)
                    .eq(UserFile::getFileName, fileName + "(" + i + ")")
                    .eq(UserFile::getIsDir, isDir);
            if (userFile.isFile()) {
                lambdaQueryWrapper1.eq(UserFile::getExtendName, extendName);
            }
            list = userFileMapper.selectList(lambdaQueryWrapper1);

        }

        return fileName + "(" + i + ")";

    }

    /**
     * 还原父文件路径至数据库，并不是在文件系统中创建文件夹
     * <p>
     * 1、回收站文件还原操作会将文件恢复到原来的路径下,当还原文件的时候，如果父目录已经不存在了，则需要把父母录给还原
     * 2、上传目录
     *
     * @param sessionUserId
     */
    @Transactional(rollbackFor = Exception.class)
    public void restoreParentFilePath(NetdiskFile netdiskFile, String sessionUserId) {

        if (netdiskFile.isFile() && netdiskFile.getParent()!=null) {
            netdiskFile = netdiskFile.getParentFile();
        }
        while (netdiskFile.getParent() != null) {
            String fileName = netdiskFile.getName();
            String parentFilePath = netdiskFile.getParent();

            LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserFile::getFilePath, parentFilePath)
                    .eq(UserFile::getFileName, fileName)
                    .eq(UserFile::getDeleteFlag, 0)
                    .eq(UserFile::getIsDir, 1)
                    .eq(UserFile::getUserId, sessionUserId);
            List<UserFile> userFileList = userFileMapper.selectList(lambdaQueryWrapper);
            if (userFileList.isEmpty()) {
                UserFile userFile = NetdiskFileUtil.getNetdiskDir(sessionUserId, parentFilePath, fileName);
                try {
                    userFileMapper.insert(userFile);
                } catch (Exception e) {
                    //ignore
                }
            }
            netdiskFile = new NetdiskFile(parentFilePath, true);
        }
    }


    /**
     * 删除重复的子目录文件
     * <p>
     * 当还原目录的时候，如果其子目录在文件系统中已存在，则还原之后进行去重操作
     *
     * @param filePath
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRepeatSubDirFile(String filePath) {
        log.debug("删除子目录：" + filePath);

        List<UserFile> repeatList = userFileMapper.selectRepeatUserFile(UserContext.getUser(), filePath);

        for (UserFile userFile : repeatList) {
            LambdaQueryWrapper<UserFile> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(UserFile::getFilePath, userFile.getFilePath())
                    .eq(UserFile::getFileName, userFile.getFileName())
                    .eq(UserFile::getDeleteFlag, "0");
            List<UserFile> userFiles = userFileMapper.selectList(lambdaQueryWrapper1);
            for (int i = 0; i < userFiles.size() - 1; i++) {
                userFileMapper.deleteById(userFiles.get(i).getUserFileId());
            }
        }
    }

    /**
     * 组织一个树目录节点，文件移动的时候使用
     *
     * @param treeNode
     * @param id
     * @param filePath
     * @param nodeNameQueue
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public TreeNode insertTreeNode(TreeNode treeNode, long id, String filePath, Queue<String> nodeNameQueue) {

        List<TreeNode> childrenTreeNodes = treeNode.getChildren();
        String currentNodeName = nodeNameQueue.peek();
        if (currentNodeName == null) {
            return treeNode;
        }

        NetdiskFile netdiskFile = new NetdiskFile(filePath, currentNodeName, true);
        filePath = netdiskFile.getPath();

        if (!isExistPath(childrenTreeNodes, currentNodeName)) {  //1、判断有没有该子节点，如果没有则插入
            //插入
            TreeNode resultTreeNode = new TreeNode();

            resultTreeNode.setFilePath(filePath);
            resultTreeNode.setLabel(nodeNameQueue.poll());
            resultTreeNode.setId(++id);

            childrenTreeNodes.add(resultTreeNode);

        } else {  //2、如果有，则跳过
            nodeNameQueue.poll();
        }

        if (nodeNameQueue.size() != 0) {
            for (int i = 0; i < childrenTreeNodes.size(); i++) {

                TreeNode childrenTreeNode = childrenTreeNodes.get(i);
                if (currentNodeName.equals(childrenTreeNode.getLabel())) {
                    childrenTreeNode = insertTreeNode(childrenTreeNode, id * 10, filePath, nodeNameQueue);
                    childrenTreeNodes.remove(i);
                    childrenTreeNodes.add(childrenTreeNode);
                    treeNode.setChildren(childrenTreeNodes);
                }

            }
        } else {
            treeNode.setChildren(childrenTreeNodes);
        }

        return treeNode;

    }

    /**
     * 判断该路径在树节点中是否已经存在
     *
     * @param childrenTreeNodes
     * @param path
     * @return
     */
    public boolean isExistPath(List<TreeNode> childrenTreeNodes, String path) {
        boolean isExistPath = false;

        try {
            for (TreeNode childrenTreeNode : childrenTreeNodes) {
                if (path.equals(childrenTreeNode.getLabel())) {
                    isExistPath = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return isExistPath;
    }


    @Transactional(rollbackFor = Exception.class)
    public void uploadESByUserFileId(String userFileId) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("user_file_id", userFileId);
            List<UserFile> userFileResult = userFileMapper.selectByMap(param);
            if (userFileResult != null && userFileResult.size() > 0) {
                FileSearch fileSearch = new FileSearch();
                BeanUtil.copyProperties(userFileResult.get(0), fileSearch);
            /*if (fileSearch.getIsDir() == 0) {

                Reader reader = ufopFactory.getReader(fileSearch.getStorageType());
                ReadFile readFile = new ReadFile();
                readFile.setFileUrl(fileSearch.getFileUrl());
                String content = reader.read(readFile);
                //全文搜索
                fileSearch.setContent(content);

            }*/
                elasticsearchClient.index(i -> i.index("filesearch")
                        .id(fileSearch.getUserFileId())
                        .document(fileSearch));
            }
        } catch (Exception e) {
            log.debug("ES更新操作失败，请检查配置");
            throw new FileOperationException("ES更新操作失败，请检查配置", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteESByUserFileId(String userFileId) {
            try {
                elasticsearchClient.delete(d -> d
                        .index("filesearch")
                        .id(userFileId));
            } catch (Exception e) {
                log.debug("ES删除操作失败，请检查配置");
            }
    }

    /**
     * 根据用户传入的参数，判断是否有下载或者预览权限
     *
     * @return
     */
    public boolean checkAuthDownloadAndPreview(String shareBatchNum,
                                               String extractionCode,
                                               String userFileIds,
                                               Integer platform) {
        log.debug("权限检查开始：shareBatchNum:{}, extractionCode:{}, userFileIds{}", shareBatchNum, extractionCode, userFileIds);
        if (platform != null && platform == 2) {
            return true;
        }
        String[] userFileIdArr = userFileIds.split(",");
        for (String userFileId : userFileIdArr) {

            UserFile userFile = userFileMapper.selectById(userFileId);
            log.debug(JSON.toJSONString(userFile));
            if ("undefined".equals(shareBatchNum) || StrUtil.isEmpty(shareBatchNum)) {
                // 自己下载或者预览自己的文件
                String userId = UserContext.getUser();
                log.debug(JSON.toJSONString("当前登录session用户id：" + userId));
                log.debug("文件所属用户id：" + userFile.getUserId());
                log.debug("登录用户id:" + userId);
                if (!userFile.getUserId().equals(userId)) {
                    log.info("用户id不一致，权限校验失败");
                    return false;
                }
            } else {
                Map<String, Object> param = new HashMap<>();
                param.put("shareBatchNum", shareBatchNum);
                List<Share> shareList = shareMapper.selectByMap(param);
                //判断批次号
                if (shareList.size() <= 0) {
                    log.info("分享批次号不存在，权限校验失败");
                    return false;
                }
                Integer shareType = shareList.get(0).getShareType();
                if (1 == shareType) {
                    //判断提取码
                    if (!shareList.get(0).getExtractionCode().equals(extractionCode)) {
                        log.info("提取码错误，权限校验失败");
                        return false;
                    }
                }
                param.put("userFileId", userFileId);
                List<ShareFile> shareFileList = shareFileService.listByMap(param);
                if (shareFileList.size() <= 0) {
                    log.info("用户id和分享批次号不匹配，权限校验失败");
                    return false;
                }

            }

        }
        return true;
    }

//    /**
//     * 拷贝文件
//     * 场景：修改的文件被多处引用时，需要重新拷贝一份，然后在新的基础上修改
//     *
//     * @param fileBean
//     * @param userFile
//     * @return
//     */
//    public String copyFile(FileBean fileBean, UserFile userFile) {
//        Copier copier = fileStorageFactory.getCopier();
//        Downloader downloader = fileStorageFactory.getDownloader(fileBean.getStorageType());
//        DownloadFile downloadFile = new DownloadFile();
//        downloadFile.setFileUrl(fileBean.getFileUrl());
//        CopyFile copyFile = new CopyFile();
//        copyFile.setExtendName(userFile.getExtendName());
//        String fileUrl = copier.copy(downloader.getInputStream(downloadFile), copyFile);
//        if (downloadFile.getOssClient() != null) {
//            downloadFile.getOssClient().shutdown();
//        }
//        fileBean.setFileUrl(fileUrl);
//        fileBean.setFileId(IdUtil.getSnowflakeNextIdStr());
//        fileMapper.insert(fileBean);
//        userFile.setFileId(fileBean.getFileId());
//        userFile.setUploadTime(DateUtil.now());
//        userFile.setModifyTime(DateUtil.now());
//        userFile.setModifyUserId(UserContext.getUser());
//        userFileMapper.updateById(userFile);
//        return fileUrl;
//    }
//
//    public String getIdentifierByFile(String fileUrl, int storageType) throws IOException {
//        DownloadFile downloadFile = new DownloadFile();
//        downloadFile.setFileUrl(fileUrl);
//        InputStream inputStream = fileStorageFactory.getDownloader(storageType).getInputStream(downloadFile);
//        return DigestUtils.md5Hex(inputStream);
//    }
//
//    public void saveFileInputStream(int storageType, String fileUrl, InputStream inputStream) throws IOException {
//        Writer writer1 = fileStorageFactory.getWriter(storageType);
//        WriteFile writeFile = new WriteFile();
//        writeFile.setFileUrl(fileUrl);
//        int fileSize = inputStream.available();
//        writeFile.setFileSize(fileSize);
//        writer1.write(inputStream, writeFile);
//    }

    public boolean isDirExist(String fileName, String filePath, String userId) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFileName, fileName)
                .eq(UserFile::getFilePath, NetdiskFile.formatPath(filePath))
                .eq(UserFile::getUserId, userId)
                .eq(UserFile::getDeleteFlag, 0)
                .eq(UserFile::getIsDir, 1);
        List<UserFile> list = userFileMapper.selectList(lambdaQueryWrapper);
        if (list != null && !list.isEmpty()) {
            return true;
        }
        return false;
    }


    public Boolean parseMusicFile(String extendName, int storageType, String fileUrl, String fileId) {
        File outFile = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
//            if ("mp3".equalsIgnoreCase(extendName) || "flac".equalsIgnoreCase(extendName)) {
//                Downloader downloader = fileStorageFactory.getDownloader(storageType);
//                DownloadFile downloadFile = new DownloadFile();
//                downloadFile.setFileUrl(fileUrl);
//                inputStream = downloader.getInputStream(downloadFile);
//                outFile = FileTypeUtil.getTempFile(fileUrl);
//                if (!outFile.exists()) {
//                    outFile.createNewFile();
//                }
//                fileOutputStream = new FileOutputStream(outFile);
//                IOUtils.copy(inputStream, fileOutputStream);
//                Music music = new Music();
//                music.setMusicId(IdUtil.getSnowflakeNextIdStr());
//                music.setFileId(fileId);
//
//                Tag tag = null;
//                AudioHeader audioHeader = null;
//                if ("mp3".equalsIgnoreCase(extendName)) {
//                    MP3File f = (MP3File) AudioFileIO.read(outFile);
//                    tag = f.getTag();
//                    audioHeader = f.getAudioHeader();
//                    MP3File mp3file = new MP3File(outFile);
//                    if (mp3file.hasID3v2Tag()) {
//                        AbstractID3v2Tag id3v2Tag = mp3file.getID3v2TagAsv24();
//                        AbstractID3v2Frame frame = (AbstractID3v2Frame) id3v2Tag.getFrame("APIC");
//                        FrameBodyAPIC body;
//                        if (frame != null && !frame.isEmpty()) {
//                            body = (FrameBodyAPIC) frame.getBody();
//                            byte[] imageData = body.getImageData();
//                            music.setAlbumImage(Base64.getEncoder().encodeToString(imageData));
//                        }
//                        if (tag != null) {
//                            music.setArtist(tag.getFirst(FieldKey.ARTIST));
//                            music.setTitle(tag.getFirst(FieldKey.TITLE));
//                            music.setAlbum(tag.getFirst(FieldKey.ALBUM));
//                            music.setYear(tag.getFirst(FieldKey.YEAR));
//                            try {
//                                music.setTrack(tag.getFirst(FieldKey.TRACK));
//                            } catch (Exception e) {
//                                // ignore
//                            }
//
//                            music.setGenre(tag.getFirst(FieldKey.GENRE));
//                            music.setComment(tag.getFirst(FieldKey.COMMENT));
//                            music.setLyrics(tag.getFirst(FieldKey.LYRICS));
//                            music.setComposer(tag.getFirst(FieldKey.COMPOSER));
//                            music.setAlbumArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
//                            music.setEncoder(tag.getFirst(FieldKey.ENCODER));
//                        }
//                    }
//                } else if ("flac".equalsIgnoreCase(extendName)) {
//                    AudioFile f = new FlacFileReader().read(outFile);
//                    tag = f.getTag();
//                    audioHeader = f.getAudioHeader();
//                    if (tag != null) {
//                        music.setArtist(StrUtil.join(",", tag.getFields(FieldKey.ARTIST)));
//                        music.setTitle(StrUtil.join(",", tag.getFields(FieldKey.TITLE)));
//                        music.setAlbum(StrUtil.join(",", tag.getFields(FieldKey.ALBUM)));
//                        music.setYear(StrUtil.join(",", tag.getFields(FieldKey.YEAR)));
//                        music.setTrack(StrUtil.join(",", tag.getFields(FieldKey.TRACK)));
//                        music.setGenre(StrUtil.join(",", tag.getFields(FieldKey.GENRE)));
//                        music.setComment(StrUtil.join(",", tag.getFields(FieldKey.COMMENT)));
//                        music.setLyrics(StrUtil.join(",", tag.getFields(FieldKey.LYRICS)));
//                        music.setComposer(StrUtil.join(",", tag.getFields(FieldKey.COMPOSER)));
//                        music.setAlbumArtist(StrUtil.join(",", tag.getFields(FieldKey.ALBUM_ARTIST)));
//                        music.setEncoder(StrUtil.join(",", tag.getFields(FieldKey.ENCODER)));
//                        List<Artwork> artworkList = tag.getArtworkList();
//                        if (artworkList != null && !artworkList.isEmpty()) {
//                            Artwork artwork = artworkList.get(0);
//                            byte[] binaryData = artwork.getBinaryData();
//                            music.setAlbumImage(Base64.getEncoder().encodeToString(binaryData));
//                        }
//                    }
//
//                }
//
//                if (audioHeader != null) {
//                    music.setTrackLength(Float.parseFloat(audioHeader.getTrackLength() + ""));
//                }
//                // 获取歌词
////                if (StrUtil.isEmpty(music.getLyrics())) {
////                    try {
////
////                        String lyc = MusicUtils.getLyc(music.getArtist(), music.getTitle(), music.getAlbum());
////                        music.setLyrics(lyc);
////                    } catch (Exception e) {
////                        log.info(e.getMessage());
////                    }
////                }
//                musicMapper.insert(music);
//            }
        } catch (Exception e) {
            log.error("解析音乐信息失败！", e);
            return false;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(fileOutputStream);
            if (outFile != null) {
                if (outFile.exists()) {
                    outFile.delete();
                }
            }
        }
        return true;
    }


    public void saveUnzipFile(String unzipDirTempUrl, String entryName, String userId, String savePath) {
        // 获取具体的子文件
        String entryFileTempUrl = unzipDirTempUrl + entryName;

        String fileId = null;
        boolean isDir = transferClient.isDirTempFile(entryFileTempUrl);
        if (!isDir) {
            // 获取文件的md5值
            String md5Str = transferClient.getMd5(entryFileTempUrl);
            // 查看是否存在同md5的文件
            Map<String, Object> param = new HashMap<>();
            param.put("identifier", md5Str);
            List<FileBean> list = fileMapper.selectByMap(param);

            if (list != null && !list.isEmpty()) { //文件已存在，不需要再向数据库中添加，也不需要将文件传入网盘区域
                fileId = list.get(0).getFileId();
            } else { //文件不存在
                // 先将文件从缓存区域传入网盘区域
                System.out.println(FilenameUtils.getExtension(entryName));
                Map<String, Object> res = transferClient.uploadTempFile2Netdisk(entryFileTempUrl, FilenameUtils.getExtension(entryName));
                String saveFileUrl = (String) res.get("fileUrl");
                Integer fileSize = (Integer) res.get("fileSize");
                System.out.println(saveFileUrl);
                System.out.println(fileSize);
                Integer storageType = Integer.parseInt(transferClient.getStorageType());
                FileBean tempFileBean = new FileBean(saveFileUrl, fileSize.longValue(), storageType, md5Str, userId);
                // 将文件信息存入数据库
                fileMapper.insert(tempFileBean);
                fileId = tempFileBean.getFileId();
            }
            transferClient.deleteTempFile(entryFileTempUrl);
        }
        // 向数据库中添加用户文件指针
        NetdiskFile netdiskFile = new NetdiskFile(savePath, entryName, isDir);
        UserFile saveUserFile = new UserFile(netdiskFile, userId, fileId);
        // 根据需要看是否需要重命名文件，比如在同级目录下有同名文件则需要重命名
        String fileName = getRepeatFileName(saveUserFile, saveUserFile.getFilePath());

        // 如果有同名文件夹则不需要再添加用户文件指针
        if (saveUserFile.getIsDir() != 1 || fileName.equals(saveUserFile.getFileName())) {
            saveUserFile.setFileName(fileName);
            userFileMapper.insert(saveUserFile);
        }

        // 递归还原父文件路径
        Map<String, String> map = new HashMap<>();
        // map.put("netdiskFile", JSON.toJSONString(netdiskFile));
        map.put("path", netdiskFile.getPath());
        map.put("isDir", netdiskFile.isDirectory()? "1": "0");
        map.put("userId", userId);
        rabbitMqHelper.sendMessage(
                MQConstants.EXCHANGE_FILE_DEAL,
                MQConstants.ROUTING_KEY_RESTORE_PARENT,
                map
        );
    }

    public void checkESUserFileId(String userFileId) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        if (userFile == null) {
            deleteESByUserFileId(userFileId);
        }
    }
}
