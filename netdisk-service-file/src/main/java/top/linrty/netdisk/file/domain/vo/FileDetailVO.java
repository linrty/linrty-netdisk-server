package top.linrty.netdisk.file.domain.vo;

import lombok.Data;
import top.linrty.netdisk.file.domain.po.Image;
import top.linrty.netdisk.file.domain.po.Music;


@Data
// TODO 添加内部成员Music和Image对应的VO类，迁移到common模块中
public class FileDetailVO {
    private String fileId;

    private String timeStampName;

    private String fileUrl;

    private Long fileSize;

    private Integer storageType;

    private Integer pointCount;

    private String identifier;

    private String userFileId;

    private Long userId;

    private String fileName;

    private String filePath;

    private String extendName;

    private Integer isDir;

    private String uploadTime;

    private Integer deleteFlag;

    private String deleteTime;

    private String deleteBatchNum;

    private Image image;

    private Music music;
}
