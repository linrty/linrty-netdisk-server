package top.linrty.netdisk.file.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "filepermission")
@Entity
@TableName("file_permission")
public class FilePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    @Column(columnDefinition="bigint(20)")
    public Long filePermissionId;

    @Column(columnDefinition="varchar(20)  comment '共享文件id'")
    public String commonFileId;

    @Column(columnDefinition="bigint(20) comment '用户id'")
    public Long userId;

    @Column(columnDefinition="int(2) comment '用户对文件的权限码'")
    public Integer filePermissionCode;

}
