package top.linrty.netdisk.config.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.config.domain.dto.QueryGroupParamDTO;
import top.linrty.netdisk.config.domain.po.SysParam;
import top.linrty.netdisk.config.service.ISysParamService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @Tag(name = "系统参数管理")
@RestController
@RequestMapping("/param")
public class SysParamController {
    @Resource
    ISysParamService sysParamService;

    // @Operation(summary = "查询系统参数组", tags = {"系统参数管理"})
    @RequestMapping(value = "/grouplist", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<Map> groupList(
            // @Parameter(description = "查询参数dto", required = false)
            QueryGroupParamDTO queryGroupParamDTO
    ) {
        List<SysParam> list = sysParamService.list(new QueryWrapper<SysParam>().lambda().eq(SysParam::getGroupName, queryGroupParamDTO.getGroupName()));
        Map<String, Object> result = new HashMap<>();

        for (SysParam sysParam : list) {
            result.put(sysParam.getSysParamKey(), sysParam.getSysParamValue());
        }

        return RestResult.success().data(result);
    }

    @RequestMapping(value = "/totalstoragesize", method = RequestMethod.GET)
    @ResponseBody
    public Long getTotalStorageSize(){
        return sysParamService.getTotalStorageSize();
    }


}
