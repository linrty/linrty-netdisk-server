package top.linrty.netdisk.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.linrty.netdisk.common.constant.Constants;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.exception.UserException;
import top.linrty.netdisk.common.util.HashUtils;
import top.linrty.netdisk.common.util.PasswordUtil;
import top.linrty.netdisk.user.util.JwtTool;
import top.linrty.netdisk.user.domain.po.UserBean;
import top.linrty.netdisk.user.domain.po.Role;
import top.linrty.netdisk.user.domain.vo.UserLoginVO;
import top.linrty.netdisk.user.mapper.UserMapper;
import top.linrty.netdisk.user.service.IUserService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, UserBean> implements IUserService {

    @Resource
    UserMapper userMapper;
    @Resource
    JwtTool jwtTool;

    @Override
    public String getUserIdByToken(String token) {
        Claims c = null;
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        token = token.replace("Bearer ", "");
        token = token.replace("Bearer%20", "");
        try {
            c = jwtTool.parseJWT(token);
        } catch (Exception e) {
            log.error("解码异常:" + e);
            return null;
        }
        if (c == null) {
            log.info("解码为空");
            return null;
        }
        String subject = c.getSubject();
        log.debug("解析结果：" + subject);
        UserBean tokenUserBean = JSON.parseObject(subject, UserBean.class);
        UserBean user = userMapper.selectById(tokenUserBean.getUserId());
        if (user != null) {
            return user.getUserId();
        }

        return null;
    }


    @Override
    @Transactional(rollbackFor=Exception.class)
    public RestResult<String> registerUser(UserBean userBean) {

        //判断验证码
        // String telephone = userBean.getTelephone();

        // UserController.verificationCodeMap.remove(telephone);
        // TODO 需要加一个验证的过程，比如手机验证码等等

        if (isUserNameExit(userBean)) {
            throw new UserException("用户名已存在！");
        }
        if (!isPhoneFormatRight(userBean.getTelephone())){
            throw new UserException("手机号格式不正确！");
        }
        if (isPhoneExit(userBean)) {
            throw new UserException("手机号已存在！");
        }

        String salt = PasswordUtil.getSaltValue();
        String newPassword = HashUtils.hashHex("MD5", userBean.getPassword(), salt, 1024);

        userBean.setSalt(salt);

        userBean.setPassword(newPassword);
        userBean.setRegisterTime(DateUtil.now());
        userBean.setUserId(IdUtil.getSnowflakeNextIdStr());
        int result = userMapper.insertUser(userBean);
        userMapper.insertUserRole(userBean.getUserId(), 2);
        if (result == 1) {
            return RestResult.success();
        } else {
            throw new UserException("注册用户失败，请检查输入信息！");
        }
    }

    @Override
    public RestResult<UserLoginVO> login(String telephone, String password) {
        RestResult<UserLoginVO> restResult = new RestResult<UserLoginVO>();
        String salt = getSaltByTelephone(telephone);
        String hashPassword = HashUtils.hashHex("MD5", password, salt, 1024);
        UserBean result = selectUserByTelephoneAndPassword(telephone, hashPassword);
        if (result == null) {
            throw new UserException("手机号或密码错误！");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("userId", result.getUserId());
        String token = "";
        try {
            token = jwtTool.createJWT(param);
        } catch (Exception e) {
            log.info("登录失败：{}", e);
            throw new UserException("创建token失败！");
        }
        UserBean sessionUserBean = findUserInfoByTelephone(telephone);
        if (sessionUserBean.getAvailable() != null && sessionUserBean.getAvailable() == 0) {
            throw new UserException("用户已被禁用！");
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtil.copyProperties(sessionUserBean, userLoginVO);
        userLoginVO.setToken("Bearer " + token);
        return RestResult.success().code(200001).data(userLoginVO);
    }

    public UserBean findUserInfoByTelephone(String telephone) {
        LambdaQueryWrapper<UserBean> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserBean::getTelephone, telephone);
        return userMapper.selectOne(lambdaQueryWrapper);

    }

    @Override
    public List<Role> selectRoleListByUserId(String userId) {
        return userMapper.selectRoleListByUserId(userId);
    }

    @Override
    public String getSaltByTelephone(String telephone) {

        return userMapper.selectSaltByTelephone(telephone);
    }
    @Override
    public UserBean selectUserByTelephoneAndPassword(String username, String password) {
        return userMapper.selectUserByTelephoneAndPassword(username, password);
    }

    private Boolean isUserNameExit(UserBean userBean) {
        LambdaQueryWrapper<UserBean> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserBean::getUsername, userBean.getUsername());
        List<UserBean> list = userMapper.selectList(lambdaQueryWrapper);
        if (list != null && !list.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检测手机号是否存在
     *
     * @param userBean
     * @return
     */
    private Boolean isPhoneExit(UserBean userBean) {

        LambdaQueryWrapper<UserBean> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserBean::getTelephone, userBean.getTelephone());
        List<UserBean> list = userMapper.selectList(lambdaQueryWrapper);
        if (list != null && !list.isEmpty()) {
            return true;
        } else {
            return false;
        }

    }

    private Boolean isPhoneFormatRight(String phone){
        boolean isRight = Pattern.matches(Constants.PASSWORD_REGEX, phone);
        return isRight;
    }

}
