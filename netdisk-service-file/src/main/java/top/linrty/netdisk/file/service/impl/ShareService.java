package top.linrty.netdisk.file.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.exception.UserException;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.dto.ShareListDTO;
import top.linrty.netdisk.file.domain.po.Share;
import top.linrty.netdisk.file.domain.po.ShareFile;
import top.linrty.netdisk.file.domain.po.UserFile;
import top.linrty.netdisk.file.domain.vo.ShareListVO;
import top.linrty.netdisk.file.mapper.ShareFileMapper;
import top.linrty.netdisk.file.mapper.ShareMapper;
import top.linrty.netdisk.file.mapper.UserFileMapper;
import top.linrty.netdisk.file.service.IShareFileService;
import top.linrty.netdisk.file.service.IShareService;
import top.linrty.netdisk.file.service.IUserFileService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class ShareService extends ServiceImpl<ShareMapper, Share> implements IShareService {

    @Resource
    ShareMapper shareMapper;

    @Resource
    UserFileMapper userFileMapper;

    @Resource
    IShareFileService shareFileService;

    @Resource
    FileDealService fileDealService;

    @Resource
    IUserFileService userFileService;

    @Override
    public List<ShareListVO> selectShareList(ShareListDTO shareListDTO) {
        Long beginCount = (shareListDTO.getCurrentPage() - 1) * shareListDTO.getPageCount();
        return shareMapper.selectShareList(shareListDTO.getShareFilePath(),
                shareListDTO.getShareBatchNum(),
                beginCount, shareListDTO.getPageCount(), UserContext.getUser());
    }

    @Override
    public int selectShareListTotalCount(ShareListDTO shareListDTO) {
        return shareMapper.selectShareListTotalCount(shareListDTO.getShareFilePath(), shareListDTO.getShareBatchNum(), UserContext.getUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shareFiles(Share share, List<String> userFileIds) {
        shareMapper.insert(share);
        List<ShareFile> shareFiles = new ArrayList<>();
        for (String userFileId : userFileIds) {
            // 获取每个文件的信息
            UserFile shareUserFile = userFileMapper.selectById(userFileId);
            if (shareUserFile.getUserId().compareTo(UserContext.getUser()) != 0){
                throw new UserException("只能分享自己的文件");
            }
            if (shareUserFile.getIsDir() == 1) {
                NetdiskFile netdiskFile = new NetdiskFile(shareUserFile.getFilePath(), shareUserFile.getFileName(), true);
                List<UserFile> childUserFiles =
                        userFileMapper.selectUserFileByLikeRightFilePath(
                                netdiskFile.getPath(),
                                UserContext.getUser()
                        );
                for (UserFile childUserFile : childUserFiles) {
                    ShareFile childShareFile =
                            ShareFile.builder()
                                    .shareFileId(IdUtil.getSnowflakeNextIdStr())
                                    .userFileId(childUserFile.getUserFileId())
                                    .shareBatchNum(share.getShareBatchNum())
                                    .shareFilePath(
                                            childUserFile.getFilePath()
                                                    .replaceFirst(
                                                            childUserFile.getFilePath().equals("/") ?
                                                                    "" : childUserFile.getFilePath()
                                                            , ""
                                                    )
                                    )
                                    .build();
                    shareFiles.add(childShareFile);
                }
            }
            ShareFile shareFile =
                    ShareFile.builder()
                            .shareFileId(IdUtil.getSnowflakeNextIdStr())
                            .userFileId(shareUserFile.getUserFileId())
                            .shareBatchNum(share.getShareBatchNum())
                            .shareFilePath("/")
                            .build();
            shareFiles.add(shareFile);
        }
        shareFileService.saveBatch(shareFiles);
    }



    @Override
    public void saveShareFiles(String[] saveShareUserFileIds, String savePath, String shareBatchNum){
        List<UserFile> saveUserFileList = new ArrayList<>();
        for (String saveShareUserFileId: saveShareUserFileIds){
            UserFile saveShareUserFile = userFileMapper.selectById(saveShareUserFileId);
            String saveShareUserFileName = saveShareUserFile.getFileName();
            String saveShareUserFilePath = saveShareUserFile.getFilePath();

            // 初始化需要保存得UserFile
            UserFile saveUserFile = new UserFile();
            BeanUtil.copyProperties(saveShareUserFile, saveUserFile);
            saveShareUserFile.setUserId(UserContext.getUser());
            String saveFileName = fileDealService.getRepeatFileName(saveShareUserFile, savePath);

            if (saveShareUserFile.isDirectory()){
                ShareFile shareFile = shareFileService.getOne(
                        new QueryWrapper<ShareFile>()
                                .lambda()
                                .eq(ShareFile::getUserFileId, saveShareUserFileId)
                                .eq(ShareFile::getShareBatchNum, shareBatchNum)
                );
                List<ShareFile> childShareFileList = shareFileService.list(
                        new QueryWrapper<ShareFile>()
                                .lambda()
                                .eq(ShareFile::getShareBatchNum, shareBatchNum)
                                .likeRight(ShareFile::getShareFilePath,
                                        NetdiskFile.formatPath(shareFile.getShareFilePath() +"/"+ saveShareUserFileName)
                                )
                );

                for (ShareFile childShareFile : childShareFileList) {
                    UserFile childShareUserFile = userFileMapper.selectById(childShareFile.getUserFileId());
                    childShareUserFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
                    childShareUserFile.setUserId(UserContext.getUser());
                    childShareUserFile.setFilePath(
                            childShareUserFile.getFilePath().replaceFirst(NetdiskFile.formatPath(saveShareUserFilePath + "/" + saveShareUserFileName),
                                    NetdiskFile.formatPath(savePath + "/" + saveFileName)));

                    saveUserFileList.add(childShareUserFile);
                    // log.info("当前文件：" + JSON.toJSONString(userFile1));
                }
            }
            saveUserFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
            saveUserFile.setUserId(UserContext.getUser());
            saveUserFile.setFilePath(savePath);
            saveUserFile.setFileName(saveFileName);
            saveUserFileList.add(saveUserFile);
        }
        userFileService.saveBatch(saveUserFileList);
    }
}
