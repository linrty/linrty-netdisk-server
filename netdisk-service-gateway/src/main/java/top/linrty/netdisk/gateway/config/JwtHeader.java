package top.linrty.netdisk.gateway.config;

import lombok.Data;

@Data
public class JwtHeader {
    private String alg;
    private String typ;
}
