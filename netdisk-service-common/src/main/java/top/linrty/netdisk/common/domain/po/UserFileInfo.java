package top.linrty.netdisk.common.domain.po;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFileInfo {
    private String userFileId;

    private String userId;

    private String fileId;

    private String fileName;

    private String filePath;

    private String extendName;

    private Integer isDir;

    private String uploadTime;

    private Integer deleteFlag;

    private String deleteTime;

    private String deleteBatchNum;

    private String createTime;

    private String createUserId;

    private String modifyTime;

    private String modifyUserId;

    public UserFileInfo(NetdiskFile netdiskFile, String userId, String fileId) {
        this.userFileId = IdUtil.getSnowflakeNextIdStr();
        this.userId = userId;
        this.fileId = fileId;
        this.filePath = netdiskFile.getParent();
        this.fileName = netdiskFile.getNameNotExtend();
        this.extendName = netdiskFile.getExtendName();
        this.isDir = netdiskFile.isDirectory() ? 1 : 0;
        String currentTime = DateUtil.now();
        this.setUploadTime(currentTime);
        this.setCreateUserId(userId);
        this.setCreateTime(currentTime);
        this.deleteFlag = 0;
    }

    public boolean isDirectory() {
        return this.isDir == 1;
    }

    public boolean isFile() {
        return this.isDir == 0;
    }

}
