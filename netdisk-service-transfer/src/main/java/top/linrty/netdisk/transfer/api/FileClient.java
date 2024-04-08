package top.linrty.netdisk.transfer.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import top.linrty.netdisk.common.domain.po.NetdiskFile;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

@FeignClient("netdisk-service-file")
public interface FileClient {
    @RequestMapping(value = "/file/add_file_point", method = RequestMethod.POST)
    public Boolean addFilePoint(@RequestParam String identifier,
                                @RequestParam String path,
                                @RequestParam Boolean isDir);

    @RequestMapping(value = "/file/add_file", method = RequestMethod.GET)
    public String addFile(@RequestParam String Url,
                          @RequestParam Long fileSize,
                          @RequestParam Integer storageType,
                          @RequestParam String identifier);

    @RequestMapping(value = "/file/add_user_file", method = RequestMethod.POST)
    public String addUserFile(@RequestParam String path,
                              @RequestParam Boolean isDir,
                              @RequestParam  String fileId,
                              @RequestParam String identifier);

    @RequestMapping(value = "/file/gen_image", method = RequestMethod.GET)
    public Boolean genImage(@RequestParam String fileId,
                            @RequestParam int width,
                            @RequestParam  int height);

    @RequestMapping(value = "/file/parse_music", method = RequestMethod.GET)
    public Boolean parseMusic(@RequestParam String extendName,
                              @RequestParam  int storageType,
                              @RequestParam String fileUrl,
                              @RequestParam String fileId);

    @RequestMapping(value = "/file/get_user_file_info_map", method = RequestMethod.GET)
    public Map<String, Object> getUserFileInfoMap(@RequestParam String userFileId);

    @RequestMapping(value = "/file/get_file_info_map", method = RequestMethod.GET)
    public Map<String, Object> getFileInfoMap(@RequestParam String fileId);

    @RequestMapping(value = "/file/get_dir_children", method = RequestMethod.GET)
    public List<String> getDirChildren(@RequestParam String dirPath,
                                       @RequestParam String userId);

    @RequestMapping(value = "/file/get_picture_file_info_map_by_file_url", method = RequestMethod.GET)
    public Map<String, Object> getPictureFileInfoMapByFileUrl(@RequestParam String fileUrl);

    @RequestMapping(value = "/storage/check_storage")
    public Boolean checkStorage(@RequestParam Long fileSize);

    @RequestMapping(value = "/file/delete_file", method = RequestMethod.GET)
    public Boolean deleteFile(@RequestParam String fileId);

    @RequestMapping(value = "/file/check_auth_download_preview", method = RequestMethod.GET)
    public Boolean checkAuthDownloadPreview(@RequestParam String shareBatchNum,
                                            @RequestParam String extractionCode,
                                            @RequestParam String userFileIds,
                                            @RequestParam Integer platform);

    @RequestMapping(value = "/file/copy_user_file", method = RequestMethod.GET)
    public void copyUserFile(@RequestParam String userFileId,
                                @RequestParam String newFilePath);

    @RequestMapping(value = "/file/delete_repeat_sub_dir_file", method = RequestMethod.GET)
    public void deleteRepeatSubDirFile(@RequestParam String filePath);

    @RequestMapping(value = "/file/get_file_info_by_identifier", method = RequestMethod.GET)
    public List<Map<String, Object>> getFileInfoByIdentifier(@RequestParam String identifier);


    @RequestMapping(value = "/file/add_file_by_file_info", method = RequestMethod.POST)
    public String addFileByFileInfo(@RequestBody Map<String, Object> fileInfo);

    @RequestMapping(value = "/file/add_user_file_by_user", method = RequestMethod.POST)
    public String addUserFileByUser(@RequestBody NetdiskFile netdiskFile, @RequestParam String userId, @RequestParam String fileId);
}
