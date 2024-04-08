package top.linrty.netdisk.file.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Table(name = "share")
@Entity
@TableName("share")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Share {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    private String shareId;
    @Column(columnDefinition="varchar(20) comment '用户id'")
    private String userId;
    @Column(columnDefinition="varchar(30) comment '分享时间'")
    private String shareTime;
    @Column(columnDefinition="varchar(30) comment '失效时间'")
    private String endTime;
    @Column(columnDefinition="varchar(10) comment '提取码'")
    private String extractionCode;
    @Column(columnDefinition="varchar(40) comment '分享批次号'")
    private String shareBatchNum;
    @Column(columnDefinition="int(2) comment '分享类型(0公共,1私密,2好友)'")
    private Integer shareType;
    @Column(columnDefinition="int(2) comment '分享状态(0正常,1已失效,2已撤销)'")
    private Integer shareStatus;

}
