package top.linrty.netdisk.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.common.domain.dto.file.ShareListDTO;
import top.linrty.netdisk.file.domain.po.Share;
import top.linrty.netdisk.common.domain.vo.file.ShareListVO;

import java.util.List;

public interface IShareService extends IService<Share> {
    List<ShareListVO> selectShareList(ShareListDTO shareListDTO);
    int selectShareListTotalCount(ShareListDTO shareListDTO);

    void shareFiles(Share share, List<String> userFileIds);

    void saveShareFiles(String[] saveShareUserFileIds, String savePath, String shareBatchNum);
}
