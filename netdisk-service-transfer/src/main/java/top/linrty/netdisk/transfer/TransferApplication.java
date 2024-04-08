package top.linrty.netdisk.transfer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import top.linrty.netdisk.common.config.DefaultFeignConfig;

@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("top.linrty.netdisk.transfer.mapper")
@SpringBootApplication
public class TransferApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransferApplication.class, args);
    }
}
