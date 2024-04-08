import cn.hutool.core.bean.BeanUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.linrty.netdisk.common.util.RedisLock;
import top.linrty.netdisk.common.util.RedisUtil;
import top.linrty.netdisk.user.domain.po.RolePermission;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@SpringBootTest(classes = top.linrty.netdisk.user.UserApplication.class)
public class Test {

    @Resource
    RedisUtil redisUtil;


    @org.junit.jupiter.api.Test
    public void testSimpleQueue(){
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(1L);
        Map<String, Object> map = BeanUtil.beanToMap(rolePermission);
        if (map.get("roleId").equals(1L)) {
            System.out.println("roleId is 1");
        } else {
            System.out.println("roleId is not 1");
        }
    }

}
