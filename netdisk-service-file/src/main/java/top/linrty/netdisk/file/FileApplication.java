package top.linrty.netdisk.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.linrty.netdisk.common.config.DefaultFeignConfig;

@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
@EnableDiscoveryClient
@MapperScan("top.linrty.netdisk.file.mapper")
@SpringBootApplication
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
