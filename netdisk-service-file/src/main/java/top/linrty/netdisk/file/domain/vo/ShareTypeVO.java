package top.linrty.netdisk.file.domain.vo;

import lombok.Data;

@Data
// @Schema(description="分享类型VO")
public class ShareTypeVO {
    // @Schema(description="0公共，1私密，2好友")
    private Integer shareType;
}
