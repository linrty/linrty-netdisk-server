package top.linrty.netdisk.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.notice.domain.dto.NoticeListDTO;
import top.linrty.netdisk.notice.domain.po.Notice;

public interface INoticeService extends IService<Notice> {


    IPage<Notice> selectUserPage(NoticeListDTO noticeListDTO);

}
