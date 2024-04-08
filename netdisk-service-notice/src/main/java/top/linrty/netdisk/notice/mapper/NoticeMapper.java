package top.linrty.netdisk.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import top.linrty.netdisk.notice.domain.dto.NoticeListDTO;
import top.linrty.netdisk.notice.domain.po.Notice;

/**
 * @author: xxxg
 * @date: 2021/11/18 11:25
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    IPage<Notice> selectPageVo(Page<?> page, @Param("noticeListDTO") NoticeListDTO noticeListDTO);

}
