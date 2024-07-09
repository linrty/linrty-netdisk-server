package top.linrty.netdisk.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.linrty.netdisk.file.domain.po.RecoveryFile;
import top.linrty.netdisk.common.domain.vo.file.RecoveryFileListVO;

import java.util.List;


public interface RecoveryFileMapper extends BaseMapper<RecoveryFile> {
    List<RecoveryFileListVO> selectRecoveryFileList(@Param("userId") String userId);
}
