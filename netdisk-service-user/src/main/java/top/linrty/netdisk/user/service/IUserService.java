package top.linrty.netdisk.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.user.domain.po.Role;
import top.linrty.netdisk.user.domain.po.UserBean;
import top.linrty.netdisk.common.domain.vo.user.UserLoginVO;

import java.util.List;

public interface IUserService extends IService<UserBean> {


    String getUserIdByToken(String token);


    /**
     * 用户注册
     *
     * @param userBean 用户信息
     * @return 结果
     */
    RestResult<String> registerUser(UserBean userBean);

    RestResult<UserLoginVO> login(String telephone, String password);



    UserBean findUserInfoByTelephone(String telephone);
    List<Role> selectRoleListByUserId(String userId);
    String getSaltByTelephone(String telephone);
    UserBean selectUserByTelephoneAndPassword(String username, String password);







}
