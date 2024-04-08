package top.linrty.netdisk.transfer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.linrty.netdisk.transfer.domain.po.UploadTaskDetail;

import java.util.List;

public interface UploadTaskDetailMapper extends BaseMapper<UploadTaskDetail> {
    List<Integer> selectUploadedChunkNumList(String identifier);
}
