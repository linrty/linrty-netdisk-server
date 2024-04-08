package top.linrty.netdisk.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.file.domain.po.ShareFile;
import top.linrty.netdisk.file.domain.vo.ShareFileListVO;
import top.linrty.netdisk.file.mapper.ShareFileMapper;
import top.linrty.netdisk.file.service.IShareFileService;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor=Exception.class)
public class ShareFileService extends ServiceImpl<ShareFileMapper, ShareFile> implements IShareFileService {
    @Resource
    ShareFileMapper shareFileMapper;

    @Override
    public List<ShareFileListVO> selectShareFileList(String shareBatchNum, String filePath) {
        return shareFileMapper.selectShareFileList(shareBatchNum, filePath);
    }

}
