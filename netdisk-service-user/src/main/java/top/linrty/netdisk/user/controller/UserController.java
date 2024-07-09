package top.linrty.netdisk.user.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.linrty.netdisk.common.anno.MyLog;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.exception.UserException;
import top.linrty.netdisk.common.util.UserContext;
import top.linrty.netdisk.common.domain.dto.user.RegisterDTO;
import top.linrty.netdisk.user.domain.po.UserBean;
import top.linrty.netdisk.user.domain.po.UserLoginInfo;
import top.linrty.netdisk.common.domain.vo.user.UserLoginVO;
import top.linrty.netdisk.user.service.IUserLoginInfoService;
import top.linrty.netdisk.user.service.IUserService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 用户的controller层
 * 1. 用户注册
 * 2. 用户登录
 * 3. 检查用户登录信息
 */
// @Tag(name = "user", description = "该接口为用户接口，主要做用户登录，注册和校验token")
@RestController
@Slf4j
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Resource
    IUserService userService;
    @Resource
    IUserLoginInfoService userLoginInfoService;

    // public static Map<String, String> verificationCodeMap = new HashMap<>();


    public static final String CURRENT_MODULE = "用户管理";

    // @Operation(summary = "用户注册", description = "注册账号", tags = {"user"})
    @PostMapping(value = "/register")
    @MyLog(operation = "用户注册", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> addUser(@Valid @RequestBody RegisterDTO registerDTO) {
        RestResult<String> restResult = null;
        UserBean userBean = new UserBean();
        BeanUtil.copyProperties(registerDTO, userBean);
        restResult = userService.registerUser(userBean);

        return restResult;
    }

    // @Operation(summary = "用户登录", description = "用户登录认证后才能进入系统", tags = {"user"})
    @GetMapping("/login")
    @MyLog(operation = "用户登录", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<UserLoginVO> userLogin(
            String telephone,
            String password){
        RestResult<UserLoginVO> restResult = null;
        restResult = userService.login(telephone, password);
        return restResult;

    }

    // @Operation(summary = "检查用户登录信息", description = "验证token的有效性", tags = {"user"})
    @GetMapping("/checkuserlogininfo")
    @ResponseBody
    public RestResult<UserLoginVO> checkUserLoginInfo() {
        UserLoginVO userLoginVO = new UserLoginVO();
        String userId = UserContext.getUser();

        if (StrUtil.isNotEmpty(userId)) {
            LambdaQueryWrapper<UserLoginInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserLoginInfo::getUserId, userId);
            lambdaQueryWrapper.likeRight(UserLoginInfo::getUserloginDate, DateUtil.now().substring(0, 10));
            userLoginInfoService.remove(lambdaQueryWrapper);
            UserLoginInfo userLoginInfo = new UserLoginInfo();
            userLoginInfo.setUserId(userId);
            userLoginInfo.setUserloginDate(DateUtil.now());
            userLoginInfoService.save(userLoginInfo);
            UserBean user = userService.getById(userId);
            BeanUtil.copyProperties(user, userLoginVO);
            if (StrUtil.isEmpty(user.getWxOpenId())) {
                userLoginVO.setHasWxAuth(false);
            } else {
                userLoginVO.setHasWxAuth(true);
            }
        } else {
            throw new UserException("用户暂未登录");
        }
        return RestResult.success().data(userLoginVO);
    }

    // @Operation(summary = "检查微信认证", description = "检查微信认证", tags = {"user"})
//    @GetMapping("/checkWxAuth")
//    @ResponseBody
//    public RestResult<Boolean> checkWxAuth() {
//        JwtUser sessionUserBean = SessionUtil.getSession();
//
//        if (sessionUserBean != null && !"anonymousUser".equals(sessionUserBean.getUsername())) {
//            UserBean user = userService.getById(sessionUserBean.getUserId());
//
//            if (StrUtil.isEmpty(user.getWxOpenId())) {
//                return RestResult.success().data(false);
//            } else {
//                return RestResult.success().data(true);
//            }
//
//        } else {
//            return RestResult.fail().message("用户暂未登录");
//        }
//
//    }

}
