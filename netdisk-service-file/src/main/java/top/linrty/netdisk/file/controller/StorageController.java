package top.linrty.netdisk.file.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.po.StorageBean;
import top.linrty.netdisk.file.service.IStorageService;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/storage")
public class StorageController {
    @Resource
    IStorageService storageService;

    @RequestMapping(value = "/getstorage", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<StorageBean> getStorage() {
        StorageBean storageBean = new StorageBean();
        storageBean.setUserId(UserContext.getUser());
        Long storageSize = storageService.selectStorageSizeByUserId(UserContext.getUser());
        StorageBean storage = new StorageBean();
        storage.setUserId(UserContext.getUser());
        storage.setStorageSize(storageSize);
        Long totalStorageSize = storageService.getTotalStorageSize(UserContext.getUser());
        storage.setTotalStorageSize(totalStorageSize);
        return RestResult.success().data(storage);
    }

    @RequestMapping(value = "/check_storage", method = RequestMethod.GET)
    @ResponseBody
    public Boolean checkStorage(Long fileSize) {
        return storageService.checkStorage(UserContext.getUser(), fileSize);
    }
}
