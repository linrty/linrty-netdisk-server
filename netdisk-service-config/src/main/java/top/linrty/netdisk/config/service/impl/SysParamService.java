package top.linrty.netdisk.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.config.domain.po.SysParam;
import top.linrty.netdisk.config.mapper.SysParamMapper;
import top.linrty.netdisk.config.service.ISysParamService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author MAC
 * @version 1.0
 * @description:
 * @date 2021/12/30 14:54
 */
@Slf4j
@Service
public class SysParamService extends ServiceImpl<SysParamMapper, SysParam> implements ISysParamService {

    @Resource
    SysParamMapper sysParamMapper;

    @Override
    public String getValue(String key) {
        SysParam sysParam = new SysParam();
        sysParam.setSysParamKey(key);
        List<SysParam> list = sysParamMapper.selectList(new QueryWrapper<>(sysParam));
        if (list != null && !list.isEmpty()) {
            return list.get(0).getSysParamValue();
        }
        return null;
    }

    @Override
    public Long getTotalStorageSize() {
        System.out.println("getTotalStorageSize："+ UserContext.getUser());
        LambdaQueryWrapper<SysParam> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(SysParam::getSysParamKey, "totalStorageSize");
        SysParam sysParam = sysParamMapper.selectOne(lambdaQueryWrapper1);
        return Long.parseLong(sysParam.getSysParamValue());
    }
}
