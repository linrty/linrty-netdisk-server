package top.linrty.netdisk.file.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("netdisk-service-transfer")
public interface TransferClient {

    @RequestMapping(value = "/filetransfer/delete_file", method = RequestMethod.GET)
    Boolean deleteFile(@RequestParam String fileId);


    @RequestMapping(value = "/filetransfer/copy_file", method = RequestMethod.GET)
    String copyFile(@RequestParam String Url, @RequestParam String targetFilePath, @RequestParam String extendName);

    @RequestMapping(value = "/filetransfer/get_storage_type", method = RequestMethod.GET)
    String getStorageType();

    @RequestMapping(value = "/filetransfer/download_file_to_temp", method = RequestMethod.GET)
    String downloadFile2Temp(@RequestParam String fileId);

    @RequestMapping(value = "/filetransfer/unzip_file", method = RequestMethod.GET)
    List<String> unzipFile(@RequestParam String unzipFileTempUrl, @RequestParam String extendName);

    @RequestMapping(value = "/filetransfer/delete_temp_file", method = RequestMethod.GET)
    void deleteTempFile(@RequestParam String tempFileUrl);


    @RequestMapping(value = "/filetransfer/upload_temp_file_to_netdisk", method = RequestMethod.GET)
    Map<String, Object> uploadTempFile2Netdisk(@RequestParam String fileUrl, @RequestParam String extendName);


    @RequestMapping(value = "/filetransfer/is_dir_temp_file", method = RequestMethod.GET)
    Boolean isDirTempFile(@RequestParam String tempFileUrl);


    @RequestMapping(value = "/filetransfer/get_md5", method = RequestMethod.GET)
    String getMd5(@RequestParam String fileUrl);

}
