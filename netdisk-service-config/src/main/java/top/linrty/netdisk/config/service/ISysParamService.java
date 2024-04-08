package top.linrty.netdisk.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.config.domain.po.SysParam;

/**
 * @author MAC
 * @version 1.0
 * @description:
 * @date 2021/12/30 14:54
 */
public interface ISysParamService extends IService<SysParam> {
    String getValue(String key);
    Long getTotalStorageSize();
}
