package top.linrty.netdisk.user.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "user_role")
@Entity
@TableName("user_role")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long userRoleId;

    @Column(columnDefinition = "varchar(20) comment '用户id'")
    private String userId;

    @Column(columnDefinition="bigint(20) comment '角色id'")
    private Long roleId;
}
