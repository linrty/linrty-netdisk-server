package top.linrty.netdisk.file.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import org.apache.catalina.User;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.file.domain.po.UserFile;

public class NetdiskFileUtil {


    public static UserFile getNetdiskDir(String userId, String filePath, String fileName) {
        UserFile userFile = new UserFile();
        userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
        userFile.setUserId(userId);
        userFile.setFileId(null);
        userFile.setFileName(fileName);
        userFile.setFilePath(NetdiskFile.formatPath(filePath));
        userFile.setExtendName(null);
        userFile.setIsDir(1);
        userFile.setUploadTime(DateUtil.now());
        userFile.setCreateUserId(UserContext.getUser());
        userFile.setCreateTime(DateUtil.now());
        userFile.setDeleteFlag(0);
        userFile.setDeleteBatchNum(null);
        return userFile;
    }

    public static UserFile getNetdiskFile(String userId, String fileId, String filePath, String fileName, String extendName) {
        UserFile userFile = new UserFile();
        userFile.setUserFileId(IdUtil.getSnowflakeNextIdStr());
        userFile.setUserId(userId);
        userFile.setFileId(fileId);
        userFile.setFileName(fileName);
        userFile.setFilePath(NetdiskFile.formatPath(filePath));
        userFile.setExtendName(extendName);
        userFile.setIsDir(0);
        userFile.setUploadTime(DateUtil.now());
        userFile.setCreateTime(DateUtil.now());
        userFile.setCreateUserId(UserContext.getUser());
        userFile.setDeleteFlag(0);
        userFile.setDeleteBatchNum(null);
        return userFile;
    }

    public static UserFile searchNetdiskFileParam(UserFile userFile) {
        UserFile param = new UserFile();
        param.setFilePath(NetdiskFile.formatPath(userFile.getFilePath()));
        param.setFileName(userFile.getFileName());
        param.setExtendName(userFile.getExtendName());
        param.setDeleteFlag(0);
        param.setUserId(userFile.getUserId());
        param.setIsDir(0);
        return param;
    }

    public static String formatLikePath(String filePath) {
        String newFilePath = filePath.replace("'", "\\'");
        newFilePath = newFilePath.replace("%", "\\%");
        newFilePath = newFilePath.replace("_", "\\_");
        return newFilePath;
    }

}
