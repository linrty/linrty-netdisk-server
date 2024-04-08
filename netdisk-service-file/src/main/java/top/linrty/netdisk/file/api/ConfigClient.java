package top.linrty.netdisk.file.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("netdisk-service-config")
public interface ConfigClient {

    @RequestMapping(value = "/param/totalstoragesize", method = RequestMethod.GET)
    public Long getTotalStorageSize();

}
