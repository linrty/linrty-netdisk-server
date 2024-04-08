package top.linrty.netdisk.common.constant;

public interface Constants {
    String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    String EMAIL_OR_PHONE_REGEX = "^([a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+)|(1[3456789]\\d{9})$";
    String PHONE_REGEX = "^1[3456789]\\d{9}$";
    String DIR_NAME_REGEX = "(?!((^(con)$)|^(con)/..*|(^(prn)$)|^(prn)/..*|(^(aux)$)|^(aux)/..*|(^(nul)$)|^(nul)/..*|(^(com)[1-9]$)|^(com)[1-9]/..*|(^(lpt)[1-9]$)|^(lpt)[1-9]/..*)|^/s+|.*/s$)(^[^/////:/*/?/\"/</>/|]{1,255}$)";
    String FILE_NAME_REGEX = "^[^\\\\/:*?\"<>|]{1,255}$";
    String PARENT_PATH_REGEX = "^(/.+?/)|(/)$";
    String MD5_REGEX = "^[0-9a-z]{32}$";
    String PASSWORD_REGEX = "^[^\\s\\u4e00-\\u9fa5]{6,20}$";
}
