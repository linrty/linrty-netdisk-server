package top.linrty.netdisk.user.config;

import lombok.Data;

@Data
public class JwtHeader {
    private String alg;
    private String typ;
}
