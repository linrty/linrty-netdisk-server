package top.linrty.netdisk.common.util;

public class UserContext {
    private static final ThreadLocal<String> tl = new ThreadLocal<>();

    /**
     * 保存当前登录用户信息到ThreadLocal
     * @param userId 用户id
     */
    public static void setUser(String userId) {
        tl.set(userId);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户id
     */
    public static String getUser() {
        return tl.get();
    }

    /**
     * 移除当前登录用户信息
     */
    public static void removeUser(){
        tl.remove();
    }
}
