package top.linrty.netdisk.common.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import feign.Logger;
import top.linrty.netdisk.common.interceptor.UserInfoRequestInterceptor;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new UserInfoRequestInterceptor();
    }
}
