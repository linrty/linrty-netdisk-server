package top.linrty.netdisk.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.linrty.netdisk.file.domain.po.ShareFile;
import top.linrty.netdisk.common.domain.vo.file.ShareFileListVO;

import java.util.List;

public interface ShareFileMapper extends BaseMapper<ShareFile> {
    List<ShareFileListVO> selectShareFileList(@Param("shareBatchNum") String shareBatchNum, @Param("shareFilePath") String filePath);
}
