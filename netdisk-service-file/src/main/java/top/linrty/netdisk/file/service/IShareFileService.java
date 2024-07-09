package top.linrty.netdisk.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.file.domain.po.ShareFile;
import top.linrty.netdisk.common.domain.vo.file.ShareFileListVO;

import java.util.List;

public interface IShareFileService extends IService<ShareFile> {

    List<ShareFileListVO> selectShareFileList(String shareBatchNum, String filePath);
}
