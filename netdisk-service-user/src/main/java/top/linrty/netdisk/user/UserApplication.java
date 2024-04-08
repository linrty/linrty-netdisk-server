package top.linrty.netdisk.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.linrty.netdisk.common.config.DefaultFeignConfig;


@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("top.linrty.netdisk.user.mapper")
@SpringBootApplication
@EnableRabbit
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
