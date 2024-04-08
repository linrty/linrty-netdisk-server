package top.linrty.netdisk.notice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.linrty.netdisk.notice.domain.dto.NoticeListDTO;
import top.linrty.netdisk.notice.domain.po.Notice;
import top.linrty.netdisk.notice.mapper.NoticeMapper;
import top.linrty.netdisk.notice.service.INoticeService;

import javax.annotation.Resource;


@Slf4j
@Service
public class NoticeService extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Resource
    NoticeMapper noticeMapper;


    @Override
    public IPage<Notice> selectUserPage(NoticeListDTO noticeListDTO) {
        Page<Notice> page = new Page<>(noticeListDTO.getPage(), noticeListDTO.getPageSize());
        return noticeMapper.selectPageVo(page, noticeListDTO);
    }
}
