package top.linrty.netdisk.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.linrty.netdisk.user.domain.po.Role;
import top.linrty.netdisk.user.domain.po.UserBean;

import java.util.List;

public interface UserMapper extends BaseMapper<UserBean> {
    int insertUser(UserBean userBean);

    int insertUserRole(@Param("userId") String userId, @Param("roleId") long roleId);

    List<Role>  selectRoleListByUserId(@Param("userId") String userId);

    String selectSaltByTelephone(@Param("telephone") String telephone);

    UserBean selectUserByTelephoneAndPassword(@Param("telephone") String telephone, @Param("password") String password);

}
