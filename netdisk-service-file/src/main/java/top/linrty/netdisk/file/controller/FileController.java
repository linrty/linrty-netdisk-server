package top.linrty.netdisk.file.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.IdUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlighterEncoder;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import top.linrty.netdisk.common.anno.MyLog;
import top.linrty.netdisk.common.constant.MQConstants;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.exception.FileOperationException;
import top.linrty.netdisk.common.util.RabbitMqHelper;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.dto.*;
import top.linrty.netdisk.file.domain.po.*;
import top.linrty.netdisk.file.domain.vo.FileListVO;
import top.linrty.netdisk.file.domain.vo.SearchFileVO;
import top.linrty.netdisk.file.service.IFileService;
import top.linrty.netdisk.file.service.IUserFileService;
import top.linrty.netdisk.file.service.impl.FileDealService;
import top.linrty.netdisk.file.util.NetdiskFileUtil;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

// @Tag(name = "file", description = "该接口为文件接口，主要用来做一些文件的基本操作，如创建目录，删除，移动，复制等。")
@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {

    @Resource
    IUserFileService userFileService;

    @Resource
    FileDealService fileDealService;

    @Resource
    IFileService fileService;

    @Resource
    RabbitMqHelper rabbitMqHelper;

    @Resource
    ElasticsearchClient elasticsearchClient;

    public static final String CURRENT_MODULE = "文件接口";


    // @Operation(summary = "获取文件列表", description = "用来做前台列表展示", tags = {"file"})
    @RequestMapping(value = "/getfilelist", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<FileListVO> getFileList(
            String fileType,
            String filePath,
            long currentPage,
            long pageCount){
        System.out.println("getfilelist get request");
        if ("0".equals(fileType)) {
            IPage<FileListVO> fileList = userFileService.userFileList(null, filePath, currentPage, pageCount);
            return RestResult.success().dataList(fileList.getRecords(), fileList.getTotal());
        } else {
            IPage<FileListVO> fileList = userFileService.getFileByFileType(Integer.valueOf(fileType), currentPage, pageCount, UserContext.getUser());
            return RestResult.success().dataList(fileList.getRecords(), fileList.getTotal());
        }
    }

    // @Operation(summary = "创建文件夹", description = "目录(文件夹)的创建", tags = {"file"})
    @RequestMapping(value = "/createFold", method = RequestMethod.POST)
    @MyLog(operation = "创建文件夹", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> createFold(@Valid @RequestBody CreateFoldDTO createFoldDto) {

        String userId = UserContext.getUser();
        String filePath = createFoldDto.getFilePath();


        boolean isDirExist = fileDealService.isDirExist(createFoldDto.getFileName(), createFoldDto.getFilePath(), userId);

        if (isDirExist) {
            return RestResult.fail().message("同名文件夹已存在");
        }

        UserFile userFile = NetdiskFileUtil.getNetdiskDir(userId, filePath, createFoldDto.getFileName());
        userFileService.save(userFile);

        rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL,
                MQConstants.ROUTING_KEY_ADD_USER_FILE_TO_ES,
                userFile.getUserFileId());
        // fileDealService.uploadESByUserFileId(userFile.getUserFileId());
        return RestResult.success();
    }


    // @Operation(summary = "创建文件", description = "创建文件", tags = {"file"})
    @ResponseBody
    @RequestMapping(value = "/createFile", method = RequestMethod.POST)
    public RestResult<Object> createFile(@Valid @RequestBody CreateFileDTO createFileDTO) {
        fileService.newFile(createFileDTO.getFilePath(), createFileDTO.getFileName(), createFileDTO.getExtendName());
        return RestResult.success().message("文件创建成功");
    }


    // @Operation(summary = "文件重命名", description = "文件重命名", tags = {"file"})
    @RequestMapping(value = "/renamefile", method = RequestMethod.POST)
    @MyLog(operation = "文件重命名", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> renameFile(@RequestBody RenameFileDTO renameFileDto) {

        UserFile userFile = userFileService.getById(renameFileDto.getUserFileId());

        List<UserFile> userFiles = userFileService.selectUserFileByNameAndPath(renameFileDto.getFileName(), userFile.getFilePath());
        if (userFiles != null && !userFiles.isEmpty()) {
            return RestResult.fail().message("同名文件已存在");
        }

        LambdaUpdateWrapper<UserFile> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFile::getFileName, renameFileDto.getFileName())
                .set(UserFile::getUploadTime, DateUtil.now())
                .eq(UserFile::getUserFileId, renameFileDto.getUserFileId());
        userFileService.update(lambdaUpdateWrapper);
        if (1 == userFile.getIsDir()) {
            List<UserFile> list = userFileService.selectUserFileByLikeRightFilePath(new NetdiskFile(userFile.getFilePath(), userFile.getFileName(), true).getPath(), UserContext.getUser());

            for (UserFile newUserFile : list) {
                String escapedPattern = Pattern.quote(new NetdiskFile(userFile.getFilePath(), userFile.getFileName(), userFile.getIsDir() == 1).getPath());
                newUserFile.setFilePath(newUserFile.getFilePath().replaceFirst(escapedPattern,
                        new NetdiskFile(userFile.getFilePath(), renameFileDto.getFileName(), userFile.getIsDir() == 1).getPath()));
                userFileService.updateById(newUserFile);
            }
        }
        rabbitMqHelper.sendMessage(MQConstants.EXCHANGE_FILE_DEAL,
                MQConstants.ROUTING_KEY_ADD_USER_FILE_TO_ES,
                renameFileDto.getUserFileId());
        return RestResult.success().message("文件重命名成功");
    }


    // @Operation(summary = "获取文件树", description = "文件移动的时候需要用到该接口，用来展示目录树", tags = {"file"})
    @RequestMapping(value = "/getfiletree", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<TreeNode> getFileTree() {
        TreeNode treeNode = userFileService.getUserFileTree();
        return RestResult.success().data(treeNode);

    }


    // @Operation(summary = "文件移动", description = "可以移动文件或者目录", tags = {"file"})
    @RequestMapping(value = "/movefile", method = RequestMethod.POST)
    @MyLog(operation = "文件移动", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> moveFile(@RequestBody MoveFileDTO moveFileDto) {
        userFileService.userFileMove(moveFileDto.getUserFileId(), moveFileDto.getFilePath());
        fileDealService.deleteRepeatSubDirFile(moveFileDto.getFilePath());
        return RestResult.success();

    }

    // @Operation(summary = "批量移动文件", description = "可以同时选择移动多个文件或者目录", tags = {"file"})
    @RequestMapping(value = "/batchmovefile", method = RequestMethod.POST)
    @MyLog(operation = "批量移动文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> batchMoveFile(@RequestBody BatchMoveFileDTO batchMoveFileDto) {

        String newFilePath = batchMoveFileDto.getFilePath();
        String userFileIds = batchMoveFileDto.getUserFileIds();
        String[] userFileIdArr = userFileIds.split(",");

        for (String userFileId : userFileIdArr) {
            userFileService.userFileMove(userFileId, newFilePath);
        }

        fileDealService.deleteRepeatSubDirFile(newFilePath);

        return RestResult.success().data("批量移动文件成功");

    }

    // @Operation(summary = "解压文件", description = "解压文件。", tags = {"file"})
    @RequestMapping(value = "/unzipfile", method = RequestMethod.POST)
    @MyLog(operation = "解压文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> unzipFile(@RequestBody UnzipFileDTO unzipFileDto) {
        try {
            fileService.unzipFile(unzipFileDto.getUserFileId(), unzipFileDto.getUnzipMode(), unzipFileDto.getFilePath());
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }

        return RestResult.success();

    }

    /**
     * 添加文件指针
     * @param identifier
     * @param path
     * @param isDir
     * @return
     */
    @RequestMapping(value = "/add_file_point", method = RequestMethod.POST)
    @ResponseBody
    public Boolean addFilePoint(String identifier, String path, Boolean isDir) {
        NetdiskFile netdiskFile = new NetdiskFile(path, isDir);
        return fileService.addFilePoint(identifier, netdiskFile);
    }

    /**
     * 添加文件
     * @param Url
     * @param fileSize
     * @param storageType
     * @param identifier
     * @return
     */
    @RequestMapping(value = "/add_file", method = RequestMethod.GET)
    @ResponseBody
    public String addFile(String Url, Long fileSize, Integer storageType, String identifier) {
        return fileService.addFile(Url, fileSize, storageType, identifier);
    }

    /**
     * 添加用户文件
     * @param path
     * @param isDir
     * @param fileId
     * @param identifier
     * @return
     */
    @RequestMapping(value = "/add_user_file", method = RequestMethod.POST)
    @ResponseBody
    public String addUserFile(String path, Boolean isDir, String fileId, String identifier) {
        NetdiskFile netdiskFile = new NetdiskFile(path, isDir);
        UserFile userFile = new UserFile(netdiskFile, UserContext.getUser(), fileId);
        return userFileService.addFile(userFile, identifier);
    }

    /**
     * 生成缩略图
     * @param fileId
     * @param width
     * @param height
     * @return
     */
    @RequestMapping(value = "/gen_image", method = RequestMethod.GET)
    @ResponseBody
    public Boolean genImage(String fileId, int width, int height) {
        Image image = Image.builder()
                .imageHeight(height)
                .imageWidth(width)
                .fileId(fileId)
                .build();
        return fileService.addImage(image);
    }


    /**
     * 解析音乐文件
     * @param extendName
     * @param storageType
     * @param fileUrl
     * @param fileId
     * @return
     */
    @RequestMapping(value = "/parse_music", method = RequestMethod.GET)
    @ResponseBody
    public Boolean parseMusic(String extendName, int storageType, String fileUrl, String fileId) {
        return fileDealService.parseMusicFile(extendName, storageType, fileUrl, fileId);
    }

    /**
     * 获取用户文件信息
     * @param userFileId
     * @return
     */
    @RequestMapping(value = "/get_user_file_info_map", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getUserFileInfoMap(String userFileId) {
        UserFile userFile = userFileService.getUserFileInfo(userFileId);
        return BeanUtil.beanToMap(userFile);
    }

    /**
     * 获取文件信息
     * @param fileId
     * @return
     */
    @RequestMapping(value = "/get_file_info_map", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getFileInfo(String fileId) {
        FileBean fileBean = fileService.getFileByFileId(fileId);
        return BeanUtil.beanToMap(fileBean);
    }

    /**
     * 获取文件夹下的所有文件
     * @param dirPath
     * @param userId
     * @return
     */
    @RequestMapping(value = "/get_dir_children", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getDirChildren(String dirPath, String userId) {
        return userFileService.getDirChildren(dirPath, userId);
    }

    /**
     * 获取图片文件信息
     * @param fileUrl
     * @return
     */
    @RequestMapping(value = "/get_picture_file_info_map_by_file_url", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getPictureFileInfoMapByFileUrl(String fileUrl) {
        PictureFile pictureFile = fileService.getPictureFileByFileUrl(fileUrl);
        return BeanUtil.beanToMap(pictureFile);
    }

    @RequestMapping(value = "/delete_file", method = RequestMethod.GET)
    @ResponseBody
    public Boolean deleteFile(String fileId) {
        return userFileService.deleteUserFile(fileId);
    }

    @RequestMapping(value = "/check_auth_download_preview", method = RequestMethod.GET)
    @ResponseBody
    public Boolean checkAuthDownloadPreview(String shareBatchNum,
                                            String extractionCode,
                                            String userFileIds,
                                            Integer platform) {
        return fileDealService.checkAuthDownloadAndPreview(shareBatchNum
                , extractionCode
                , userFileIds
                , platform);
    }

    @RequestMapping(value = "/copy_user_file", method = RequestMethod.GET)
    @ResponseBody
    public void copyUserFile(String userFileId, String newFilePath) {
        userFileService.userFileCopy(userFileId, newFilePath);
    }

    @RequestMapping(value = "/delete_repeat_sub_dir_file", method = RequestMethod.GET)
    @ResponseBody
    public void deleteRepeatSubDirFile(String filePath){
        fileDealService.deleteRepeatSubDirFile(filePath);
    }

    @RequestMapping(value = "/get_file_info_by_identifier", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getFileByIdentifier(String identifier) {
        fileService.getFileByIdentifier(identifier);
        List<Map<String, Object>>res = new ArrayList<>();
        for (FileBean fileBean : fileService.getFileByIdentifier(identifier)) {
            res.add(BeanUtil.beanToMap(fileBean));
        }
        return res;
    }


    @RequestMapping(value = "/add_file_by_file_info", method = RequestMethod.POST)
    @ResponseBody
    public String addFileByFileInfo(@RequestBody Map<String,Object> fileInfo) {
        FileBean fileBean = BeanUtil.toBean(fileInfo, FileBean.class);
        fileService.save(fileBean);
        return fileBean.getFileId();
    }

    @RequestMapping(value = "/add_user_file_by_user", method = RequestMethod.POST)
    @ResponseBody
    public String addUserFileInfo(@RequestBody NetdiskFile netdisk, @RequestParam String userId, @RequestParam String fileId) {
        UserFile userFile = new UserFile(netdisk, userId, fileId);
        fileDealService.getRepeatFileName(userFile, netdisk.getPath());
        String fileName = fileDealService.getRepeatFileName(userFile, userFile.getFilePath());

        // 如果是文件，自动会获取重命名的新名字
        // 如果是文件夹并且文件名相同说明在该路径下没有同名的文件夹，否则有同名的文件夹就不需要再新建一个文件夹了跳过这步即可
        if (userFile.getIsDir() != 1 || fileName.equals(userFile.getFileName())) {
            userFile.setFileName(fileName);
            userFileService.save(userFile);
        }
        return userFile.getUserFileId();
    }

    // @Operation(summary = "文件搜索", description = "文件搜索", tags = {"file"})
    @GetMapping(value = "/search")
    @MyLog(operation = "文件搜索", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<SearchFileVO> searchFile(SearchFileDTO searchFileDTO) {

        int currentPage = (int)searchFileDTO.getCurrentPage() - 1;
        int pageCount = (int)(searchFileDTO.getPageCount() == 0 ? 10 : searchFileDTO.getPageCount());

        SearchResponse<FileSearch> search = null;
        try {
            search = elasticsearchClient.search(s -> s
                            .index("filesearch")
                            .query(_1 -> _1
                                    .bool(_2 -> _2
                                            .must(_3 -> _3
                                                    .bool(_4 -> _4
                                                            .should(_5 -> _5
                                                                    .match(_6 -> _6
                                                                            .field("fileName")
                                                                            .query(searchFileDTO.getFileName())
                                                                    )
                                                            )
                                                            .should(_5 -> _5
                                                                    .wildcard(_6 -> _6
                                                                            .field("fileName")
                                                                            .wildcard("*" + searchFileDTO.getFileName() + "*")
                                                                    )
                                                            )
                                                            .should(_5 -> _5
                                                                    .match(_6 -> _6
                                                                            .field("content")
                                                                            .query(searchFileDTO.getFileName())
                                                                    )
                                                            )
                                                            .should(_5 -> _5
                                                                    .wildcard(_6 -> _6
                                                                            .field("content")
                                                                            .wildcard("*" + searchFileDTO.getFileName() + "*")
                                                                    )
                                                            )
                                                    )
                                            ).must(_3 -> _3
                                                    .term(_4 -> _4
                                                            .field("userId")
                                                            .value(UserContext.getUser())
                                                    )
                                            )
                                    )
                            )
                            .from(currentPage)
                            .size(pageCount)
                            .highlight(h -> h.fields("fileName",
                                                    f -> f.type("plain")
                                                            .preTags("<span class='keyword'>")
                                                            .postTags("</span>")
                                    ).encoder(HighlighterEncoder.Html)
                            ),
                    FileSearch.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchFileVO> searchFileVOList = new ArrayList<>();
        for (Hit<FileSearch> hit : search.hits().hits()) {
            SearchFileVO searchFileVO = new SearchFileVO();
            BeanUtil.copyProperties(hit.source(), searchFileVO);
            searchFileVO.setHighLight(hit.highlight());
            searchFileVOList.add(searchFileVO);
            rabbitMqHelper.sendMessage(
                    MQConstants.EXCHANGE_FILE_DEAL,
                    MQConstants.ROUTING_KEY_CHECK_ES_USER_FILE,
                    searchFileVO.getUserFileId()
            );
        }
        return RestResult.success().dataList(searchFileVOList, searchFileVOList.size());
    }



}
