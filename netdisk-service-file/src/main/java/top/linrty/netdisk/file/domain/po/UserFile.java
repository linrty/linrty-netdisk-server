package top.linrty.netdisk.file.domain.po;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.linrty.netdisk.common.domain.po.NetdiskFile;
import top.linrty.netdisk.common.domain.po.UserFileInfo;

import javax.persistence.*;

@Data
@Table(name = "user_file", uniqueConstraints = {
        @UniqueConstraint(name = "fileindex", columnNames = {"userId", "filePath", "fileName", "extendName", "deleteFlag", "isDir"})
})
@Entity
@TableName("user_file")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private String userFileId;

    @Column(columnDefinition = "bigint(20) comment '用户id'")
    private String userId;

    @Column(columnDefinition="varchar(20) comment '文件id'")
    private String fileId;

    @Column(columnDefinition="varchar(100) comment '文件名'")
    private String fileName;

    @Column(columnDefinition="varchar(500) comment '文件路径'")
    private String filePath;

    @Column(columnDefinition="varchar(100) NULL DEFAULT '' comment '扩展名'")
    private String extendName;

    @Column(columnDefinition="int(1) comment '是否是目录(0-否,1-是)'")
    private Integer isDir;

    @Column(columnDefinition="varchar(25) comment '修改时间'")
    private String uploadTime;

    @Column(columnDefinition="int(11) comment '删除标识(0-未删除，1-已删除)'")
    private Integer deleteFlag;

    @Column(columnDefinition="varchar(25) comment '删除时间'")
    private String deleteTime;

    @Column(columnDefinition = "varchar(50) comment '删除批次号'")
    private String deleteBatchNum;

    @Column(columnDefinition="varchar(30) comment '创建时间'")
    private String createTime;

    @Column(columnDefinition="varchar(20) comment '创建用户id'")
    private String createUserId;

    @Column(columnDefinition="varchar(30) comment '修改时间'")
    private String modifyTime;

    @Column(columnDefinition="varchar(20) comment '修改用户id'")
    private String modifyUserId;

    public UserFile(NetdiskFile netdiskFile, String userId, String fileId) {
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

    public UserFile parseUserFileInfo(UserFileInfo userFileInfo){
        return UserFile.builder()
                .userFileId(userFileInfo.getUserFileId())
                .userId(userFileInfo.getUserId())
                .fileId(userFileInfo.getFileId())
                .fileName(userFileInfo.getFileName())
                .filePath(userFileInfo.getFilePath())
                .extendName(userFileInfo.getExtendName())
                .isDir(userFileInfo.getIsDir())
                .uploadTime(userFileInfo.getUploadTime())
                .deleteFlag(userFileInfo.getDeleteFlag())
                .deleteTime(userFileInfo.getDeleteTime())
                .deleteBatchNum(userFileInfo.getDeleteBatchNum())
                .createTime(userFileInfo.getCreateTime())
                .createUserId(userFileInfo.getCreateUserId())
                .modifyTime(userFileInfo.getModifyTime())
                .modifyUserId(userFileInfo.getModifyUserId())
                .build();
    }

}
