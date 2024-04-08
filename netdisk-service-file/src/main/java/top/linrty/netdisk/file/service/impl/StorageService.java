package top.linrty.netdisk.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.file.api.ConfigClient;
import top.linrty.netdisk.file.domain.po.StorageBean;
import top.linrty.netdisk.file.mapper.StorageMapper;
import top.linrty.netdisk.file.mapper.UserFileMapper;
import top.linrty.netdisk.file.service.IStorageService;

import javax.annotation.Resource;

@Slf4j
@Service
public class StorageService extends ServiceImpl<StorageMapper, StorageBean> implements IStorageService {
    @Resource
    StorageMapper storageMapper;

    @Resource
    ConfigClient configClient;

    @Resource
    UserFileMapper userFileMapper;

    public Long getTotalStorageSize(String userId) {
        LambdaQueryWrapper<StorageBean> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StorageBean::getUserId, userId);

        StorageBean storageBean = storageMapper.selectOne(lambdaQueryWrapper);
        Long totalStorageSize = null;
        if (storageBean == null || storageBean.getTotalStorageSize() == null) {
            // 远程调用获取总存储大小
            totalStorageSize = configClient.getTotalStorageSize();
            storageBean = new StorageBean();
            storageBean.setUserId(userId);
            storageBean.setTotalStorageSize(totalStorageSize);
            storageMapper.insert(storageBean);
        } else  {
            totalStorageSize = storageBean.getTotalStorageSize();
        }

        if (totalStorageSize != null) {
            totalStorageSize = totalStorageSize * 1024 * 1024;
        }
        return totalStorageSize;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean checkStorage(String userId, Long fileSize) {
        LambdaQueryWrapper<StorageBean> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StorageBean::getUserId, userId);

        StorageBean storageBean = storageMapper.selectOne(lambdaQueryWrapper);
        Long totalStorageSize = null;
        if (storageBean == null || storageBean.getTotalStorageSize() == null) {
            // 远程调用获取总存储大小
            totalStorageSize = configClient.getTotalStorageSize();
            storageBean = new StorageBean();
            storageBean.setUserId(userId);
            storageBean.setTotalStorageSize(totalStorageSize);
            storageMapper.insert(storageBean);
        } else  {
            totalStorageSize = storageBean.getTotalStorageSize();
        }

        if (totalStorageSize != null) {
            totalStorageSize = totalStorageSize * 1024 * 1024;
        }

        Long storageSize = userFileMapper.selectStorageSizeByUserId(userId);
        if (storageSize == null ){
            storageSize = 0L;
        }
        if (storageSize + fileSize > totalStorageSize) {
            return false;
        }
        return true;

    }

    /**
     * 查询用户存储大小
     * @param userId
     * @return
     */
    @Override
    public Long selectStorageSizeByUserId(String userId){
        return userFileMapper.selectStorageSizeByUserId(userId);
    }
}
