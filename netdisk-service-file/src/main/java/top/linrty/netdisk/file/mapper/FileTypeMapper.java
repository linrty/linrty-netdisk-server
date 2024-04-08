package top.linrty.netdisk.file.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.linrty.netdisk.file.domain.po.FileType;

import java.util.List;

public interface FileTypeMapper extends BaseMapper<FileType> {
    List<String> selectExtendNameByFileType(@Param("fileTypeId") Integer fileTypeId);

}
