package top.linrty.netdisk.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.linrty.netdisk.file.domain.po.Share;
import top.linrty.netdisk.file.domain.vo.ShareListVO;

import java.util.List;

public interface ShareMapper extends BaseMapper<Share> {

    List<ShareListVO> selectShareList(String shareFilePath, String shareBatchNum, Long beginCount, Long pageCount, String userId);
    int selectShareListTotalCount(String shareFilePath,String shareBatchNum, String userId);
}
