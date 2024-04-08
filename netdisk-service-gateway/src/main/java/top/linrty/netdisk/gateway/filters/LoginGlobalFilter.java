package top.linrty.netdisk.gateway.filters;

import cn.hutool.core.text.AntPathMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.linrty.netdisk.gateway.config.AuthProperties;
import top.linrty.netdisk.gateway.util.JwtTool;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LoginGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final AntPathMatcher antPathMatcher=new AntPathMatcher();

    private final JwtTool jwtTool;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest request = exchange.getRequest();
        //判断请求是否要拦截
        if (isAllowPath(request)){
            return chain.filter(exchange);
        }

        // 获取token
        String token=null;
        List<String> headers = request.getHeaders().get("token");
        if (headers!=null){
            token = headers.get(0);
        }
        //要拦截,解析token
        String userId =null;
        try {
            userId = jwtTool.getUserIdByToken(token);
            System.out.println("userId = "+userId);
        }catch (Exception e){
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(HttpStatus.UNAUTHORIZED.value());
            return response.setComplete();
        }
        if (userId==null){
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(HttpStatus.UNAUTHORIZED.value());
            return response.setComplete();
        }
        //传递用户到服务
        String userInfo = userId;
        ServerWebExchange exc = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        //放行
        return chain.filter(exc);
    }

    private boolean isAllowPath(ServerHttpRequest request) {
        boolean flag=false;
        // String method=request.getMethodValue();
        String path=request.getPath().toString();
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath,path)){
                flag=true;
                break;
            }
        }
        return flag;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
