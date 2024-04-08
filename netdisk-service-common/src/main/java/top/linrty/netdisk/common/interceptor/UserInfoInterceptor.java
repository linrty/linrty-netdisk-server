package top.linrty.netdisk.common.interceptor;

import cn.hutool.core.util.StrUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import top.linrty.netdisk.common.util.UserContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取用户信息
        String userInfo = request.getHeader("user-info");
        System.out.println("userInfo = "+userInfo);
        //判断是否为空,存入ThreadLocal
        if(StrUtil.isNotBlank(userInfo)){
            UserContext.setUser(String.valueOf(userInfo));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
