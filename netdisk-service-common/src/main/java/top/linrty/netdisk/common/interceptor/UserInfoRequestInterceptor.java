package top.linrty.netdisk.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import top.linrty.netdisk.common.util.UserContext;

public class UserInfoRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String userInfo = UserContext.getUser();
        if (userInfo != null) {
            requestTemplate.header("user-info", userInfo);
        }
    }
}
