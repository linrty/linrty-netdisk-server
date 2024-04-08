package top.linrty.netdisk.file.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.linrty.netdisk.common.anno.MyLog;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.dto.BatchDeleteRecoveryFileDTO;
import top.linrty.netdisk.file.domain.dto.DeleteRecoveryFileDTO;
import top.linrty.netdisk.file.domain.dto.RestoreFileDTO;
import top.linrty.netdisk.file.domain.po.RecoveryFile;
import top.linrty.netdisk.file.domain.vo.RecoveryFileListVO;
import top.linrty.netdisk.file.service.IRecoveryFileService;

import javax.annotation.Resource;
import java.util.List;

// @Tag(name = "recoveryfile", description = "文件删除后会进入回收站，该接口主要是对回收站文件进行管理")
@RestController
@Slf4j
@RequestMapping("/recoveryfile")
public class RecoveryFileController {
    @Resource
    IRecoveryFileService recoveryFileService;

    public static final String CURRENT_MODULE = "回收站文件接口";

    // @Operation(summary = "回收文件列表", description = "回收文件列表", tags = {"recoveryfile"})
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<RecoveryFileListVO> getRecoveryFileList() {
        List<RecoveryFileListVO> recoveryFileList = recoveryFileService.selectRecoveryFileList();
        return RestResult.success().dataList(recoveryFileList, recoveryFileList.size());
    }

    // @Operation(summary = "还原文件", description = "还原文件", tags = {"recoveryfile"})
    @RequestMapping(value = "/restorefile", method = RequestMethod.POST)
    @MyLog(operation = "还原文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult restoreFile(@RequestBody RestoreFileDTO restoreFileDto) {
        recoveryFileService.restoreFile(restoreFileDto.getDeleteBatchNum(), restoreFileDto.getFilePath());
        return RestResult.success().message("还原成功！");
    }

    // @Operation(summary = "删除回收文件", description = "删除回收文件", tags = {"recoveryfile"})
    @MyLog(operation = "删除回收文件", module = CURRENT_MODULE)
    @RequestMapping(value = "/deleterecoveryfile", method = RequestMethod.POST)
    @ResponseBody
    public RestResult<String> deleteRecoveryFile(@RequestBody DeleteRecoveryFileDTO deleteRecoveryFileDTO) {
        recoveryFileService.deleteRecoveryFile(deleteRecoveryFileDTO.getUserFileId());
        return RestResult.success().data("删除成功");
    }

    // @Operation(summary = "批量删除回收文件", description = "批量删除回收文件", tags = {"recoveryfile"})
    @RequestMapping(value = "/batchdelete", method = RequestMethod.POST)
    @MyLog(operation = "批量删除回收文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> batchDeleteRecoveryFile(@RequestBody BatchDeleteRecoveryFileDTO batchDeleteRecoveryFileDTO) {
        String userFileIds = batchDeleteRecoveryFileDTO.getUserFileIds();
        recoveryFileService.batchDeleteRecoveryFile(userFileIds);
        return RestResult.success().data("批量删除成功");
    }
}
