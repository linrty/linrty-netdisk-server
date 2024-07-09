package top.linrty.netdisk.common.enums;

public enum UploadFileStatusEnum {
    FAIL(0, "上传失败"),
    SUCCESS(1, "上传成功"),
    UNCOMPLATE(2, "未完成");

    private final int code;
    private final String message;

    private UploadFileStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
