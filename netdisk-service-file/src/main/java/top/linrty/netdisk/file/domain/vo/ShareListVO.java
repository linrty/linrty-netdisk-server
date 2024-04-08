package top.linrty.netdisk.file.domain.vo;

import lombok.Data;

// @Schema(description="分享列表VO")
@Data
public class ShareListVO {
    private String shareId;

    private String userId;

    private String shareTime;

    private String endTime;

    private String extractionCode;

    private String shareBatchNum;

    private Integer shareType;//0公共，1私密，2好友

    private Integer shareStatus;//0正常，1已失效，2已撤销

    private String shareFileId;

    private String userFileId;

    private String shareFilePath;

    private String fileId;

    private String fileName;

    private String filePath;

    private String extendName;

    private Integer isDir;

    private String uploadTime;

    private Integer deleteFlag;

    private String deleteTime;

    private String deleteBatchNum;

    private String timeStampName;

    private String fileUrl;

    private Long fileSize;

    private Integer storageType;
}
