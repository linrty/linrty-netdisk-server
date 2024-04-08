package top.linrty.netdisk.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.notice.domain.dto.NoticeListDTO;

import top.linrty.netdisk.notice.domain.po.Notice;
import top.linrty.netdisk.notice.service.INoticeService;

import javax.annotation.Resource;

// @Tag(name = "公告管理")
@RestController
@RequestMapping("/notice")
public class NoticeController {
    public static final String CURRENT_MODULE = "公告管理";
    @Resource
    INoticeService noticeService;

    /**
     * 得到所有的公告
     *
     * @return
     */
    // @Operation(summary = "得到所有的公告列表", tags = {"公告管理"})
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<NoticeListDTO> selectUserList(  @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int pageSize,
                                                      @RequestParam(required = false) String title,
                                                      @RequestParam(required = false) Long publisher,
                                                      @RequestParam(required = false) String beginTime,
                                                      @RequestParam(required = false) String endTime) {
        NoticeListDTO noticeListDTO = new NoticeListDTO();
        noticeListDTO.setPage(page);
        noticeListDTO.setPageSize(pageSize);
        noticeListDTO.setTitle(title);
        noticeListDTO.setPlatform(3);
        noticeListDTO.setPublisher(publisher);
        noticeListDTO.setBeginTime(beginTime);
        noticeListDTO.setEndTime(endTime);
        IPage<Notice> noticeIPage = noticeService.selectUserPage(noticeListDTO);

        return RestResult.success().dataList(noticeIPage.getRecords(), noticeIPage.getTotal());
    }

    // @Operation(summary = "查询公告详情", tags = {"公告管理"})
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<Notice> getNoticeDetail(long noticeId) {

        Notice notice = noticeService.getById(noticeId);

        return RestResult.success().data(notice);
    }




}
