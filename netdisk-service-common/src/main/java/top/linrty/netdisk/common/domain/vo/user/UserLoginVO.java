package top.linrty.netdisk.common.domain.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(name = "用户登录Vo", required = true)
public class UserLoginVO {
    @Schema(description = "用户id", example = "1")
    private String userId;

    //    @Schema(description = "openId", example = "")
//    private String openId;

    @Schema(description = "用户名", example = "netdisk")
    private String username;

    @Schema(description = "真实名", example = "张三")
    private String realname;

    @Schema(description = "qq用户名", example = "水晶之恋")
    private String qqUsername;

    @Schema(description = "qq用户头像", example = "https://example.com")
    private String qqImageUrl;

    @Schema(description = "手机号", example = "18111111111")
    private String telephone;

    @Schema(description = "邮箱", example = "111111111@qq.com")
    private String email;

    @Schema(description = "性别", example = "男")
    private String sex;

    @Schema(description = "生日", example = "1994-05-06")
    private String birthday;

    @Schema(description = "省", example = "陕西省")
    private String addrProvince;

    @Schema(description = "市", example = "西安市")
    private String addrCity;

    @Schema(description = "区", example = "雁塔区")
    private String addrArea;

    @Schema(description = "行业", example = "计算机行业")
    private String industry;

    @Schema(description = "职位", example = "java开发")
    private String position;

    @Schema(description = "个人介绍", example = "错把陈醋当成墨，写尽半生都是酸")
    private String intro;

    @Schema(description = "用户头像地址", example = "\\upload\\20200405\\93811586079860974.png")
    private String imageUrl;

    @Schema(description = "注册时间", example = "2019-12-23 14:21:52")
    private String registerTime;

    @Schema(description = "最后登录时间", example = "2019-12-23 14:21:52")
    private String lastLoginTime;

    @Schema(description = "Token 接口访问凭证")
    private String token;

    @Schema(description = "是否微信认证")
    private boolean hasWxAuth;

}
