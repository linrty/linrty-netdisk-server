package top.linrty.netdisk.file.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.linrty.netdisk.file.domain.po.FileBean;

import java.util.List;

public interface FileMapper extends BaseMapper<FileBean> {

    void batchInsertFile(List<FileBean> fileBeanList);

}
