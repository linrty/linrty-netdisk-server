package top.linrty.netdisk.file.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;


@Data
@Table(name = "fileextend")
@Entity
@TableName("file_extend")
public class FileExtend {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    @Column(columnDefinition="varchar(25)")
    private String fileExtendName;

    @Column(columnDefinition="varchar(25) comment '文件扩展名描述'")
    private String fileExtendDesc;

    @Column(columnDefinition="varchar(100) comment '文件扩展名预览图'")
    private String fileExtendImgUrl;
}
