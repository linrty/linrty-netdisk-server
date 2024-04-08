package top.linrty.netdisk.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.file.domain.po.StorageBean;

public interface IStorageService extends IService<StorageBean> {
    Long getTotalStorageSize(String userId);
    boolean checkStorage(String userId, Long fileSize);

    Long selectStorageSizeByUserId(String userId);
}
