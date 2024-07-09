package top.linrty.netdisk.file.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.linrty.netdisk.common.anno.MyLog;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.domain.dto.file.*;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.po.Share;
import top.linrty.netdisk.common.domain.vo.file.ShareFileListVO;
import top.linrty.netdisk.common.domain.vo.file.ShareFileVO;
import top.linrty.netdisk.common.domain.vo.file.ShareListVO;
import top.linrty.netdisk.common.domain.vo.file.ShareTypeVO;
import top.linrty.netdisk.file.service.IShareFileService;
import top.linrty.netdisk.file.service.IShareService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

// @Tag(name = "share", description = "该接口为文件分享接口")
@RestController
@Slf4j
@RequestMapping("/share")
public class ShareController {
    public static final String CURRENT_MODULE = "文件分享";

    @Resource
    IShareService shareService;

    @Resource
    IShareFileService shareFileService;


    // @Operation(summary = "查看已分享列表", description = "查看已分享列表", tags = {"share"})
    @GetMapping(value = "/shareList")
    @ResponseBody
    public RestResult<ShareListVO> shareList(ShareListDTO shareListDTO) {
        List<ShareListVO> shareList = shareService.selectShareList(shareListDTO);

        int total = shareService.selectShareListTotalCount(shareListDTO);

        return RestResult.success().dataList(shareList, total);
    }


    // @Operation(summary = "分享文件", description = "分享文件统一接口", tags = {"share"})
    @PostMapping(value = "/sharefile")
    @MyLog(operation = "分享文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<ShareFileVO> shareFile(@RequestBody ShareFileDTO shareFileDTO) {
        ShareFileVO shareSecretVO = new ShareFileVO();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        Share share = Share.builder()
                .shareId(IdUtil.getSnowflakeNextIdStr())
                .shareType(shareFileDTO.getShareType())
                .shareTime(DateUtil.now())
                .userId(UserContext.getUser())
                .shareStatus(0)
                .shareBatchNum(uuid)
                .endTime(shareFileDTO.getEndTime())
                .build();
        shareSecretVO.setShareBatchNum(share.getShareBatchNum());
        if (shareFileDTO.getShareType() == 1) {
            String extractionCode = RandomUtil.randomNumbers(6);
            share.setExtractionCode(extractionCode);
            shareSecretVO.setExtractionCode(share.getExtractionCode());
        }
        String userFileIds = shareFileDTO.getUserFileIds();
        String[] userFileIdArray = userFileIds.split(",");
        List<String> userFileIdList = userFileIdArray.length > 0 ? List.of(userFileIdArray) : new ArrayList<>();
        shareService.shareFiles(share, userFileIdList);
        return RestResult.success().data(shareSecretVO);
    }


    // @Operation(summary = "分享文件列表", description = "分享列表", tags = {"share"})
    @GetMapping(value = "/sharefileList")
    @ResponseBody
    public RestResult<ShareFileListVO> shareFileList(ShareFileListDTO shareFileListBySecretDTO) {
        String shareBatchNum = shareFileListBySecretDTO.getShareBatchNum();
        String shareFilePath = shareFileListBySecretDTO.getShareFilePath();
        List<ShareFileListVO> list = shareFileService.selectShareFileList(shareBatchNum, shareFilePath);
        for (ShareFileListVO shareFileListVO : list) {
            shareFileListVO.setShareFilePath(shareFilePath);
        }
        return RestResult.success().dataList(list, list.size());
    }


    // @Operation(summary = "分享类型", description = "可用此接口判断是否需要提取码", tags = {"share"})
    @GetMapping(value = "/sharetype")
    @ResponseBody
    public RestResult<ShareTypeVO> shareType(ShareTypeDTO shareTypeDTO) {
        LambdaQueryWrapper<Share> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Share::getShareBatchNum, shareTypeDTO.getShareBatchNum());
        Share share = shareService.getOne(lambdaQueryWrapper);
        ShareTypeVO shareTypeVO = new ShareTypeVO();
        shareTypeVO.setShareType(share.getShareType());
        return RestResult.success().data(shareTypeVO);
    }

    // @Operation(summary = "校验提取码", description = "校验提取码", tags = {"share"})
    @GetMapping(value = "/checkextractioncode")
    @ResponseBody
    public RestResult<String> checkExtractionCode(CheckExtractionCodeDTO checkExtractionCodeDTO) {
        LambdaQueryWrapper<Share> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Share::getShareBatchNum, checkExtractionCodeDTO.getShareBatchNum())
                .eq(Share::getExtractionCode, checkExtractionCodeDTO.getExtractionCode());
        List<Share> list = shareService.list(lambdaQueryWrapper);
        if (list.isEmpty()) {
            return RestResult.fail().message("校验失败");
        } else {
            return RestResult.success();
        }
    }

    // @Operation(summary = "校验过期时间", description = "校验过期时间", tags = {"share"})
    @GetMapping(value = "/checkendtime")
    @ResponseBody
    public RestResult<String> checkEndTime(CheckEndTimeDTO checkEndTimeDTO) {
        LambdaQueryWrapper<Share> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Share::getShareBatchNum, checkEndTimeDTO.getShareBatchNum());
        Share share = shareService.getOne(lambdaQueryWrapper);
        if (share == null) {
            return RestResult.fail().message("文件不存在！");
        }
        String endTime = share.getEndTime();
        Date endTimeDate = null;
        endTimeDate = DateUtil.parse(endTime, "yyyy-MM-dd HH:mm:ss");
        if (new Date().after(endTimeDate))  {
            return RestResult.fail().message("分享已过期");
        } else {
            return RestResult.success();
        }
    }

    // @Operation(summary = "保存分享文件", description = "用来将别人分享的文件保存到自己的网盘中", tags = {"share"})
    @PostMapping(value = "/savesharefile")
    @MyLog(operation = "保存分享文件", module = CURRENT_MODULE)
    @Transactional(rollbackFor=Exception.class)
    @ResponseBody
    public RestResult saveShareFile(@RequestBody SaveShareFileDTO saveShareFileDTO) {
        String[] saveShareUserFileIds = saveShareFileDTO.getUserFileIds().split(",");
        shareService.saveShareFiles(saveShareUserFileIds, saveShareFileDTO.getFilePath(), saveShareFileDTO.getShareBatchNum());
        return RestResult.success();
    }
}
