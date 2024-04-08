package top.linrty.netdisk.notice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.linrty.netdisk.common.config.DefaultFeignConfig;


@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("top.linrty.netdisk.notice.mapper")
@SpringBootApplication
public class NoticeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NoticeApplication.class, args);
    }
}
