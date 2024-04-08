package top.linrty.netdisk.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.exceptions.StreamingNotifiable;
import org.apache.ibatis.annotations.Param;
import top.linrty.netdisk.file.domain.po.UserFile;
import top.linrty.netdisk.file.domain.vo.FileListVO;

import java.util.List;

public interface UserFileMapper extends BaseMapper<UserFile> {

    List<UserFile> selectUserFileByLikeRightFilePath(@Param("filePath") String filePath, @Param("userId") String userId);

    IPage<FileListVO> selectPageVo(Page<?> page, @Param("userFile") UserFile userFile, @Param("fileTypeId") Integer fileTypeId);
    Long selectStorageSizeByUserId(@Param("userId") String userId);

    Long selectFilePointCount(@Param("fileId") String fileId);

    List<UserFile> selectRepeatUserFile(@Param("userId")String userId, @Param("filePath") String filePath);
}
