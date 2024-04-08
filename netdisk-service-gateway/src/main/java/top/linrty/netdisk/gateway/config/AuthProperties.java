package top.linrty.netdisk.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "netdisk.auth")
@Component
public class AuthProperties {
    private List<String> includePaths;
    private List<String> excludePaths;
}
