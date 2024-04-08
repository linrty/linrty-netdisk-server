package top.linrty.netdisk.common.exception;

import lombok.Data;
import lombok.Getter;
import top.linrty.netdisk.common.enums.ResultCodeEnum;

@Getter
public class CommonException extends RuntimeException {

    private final Integer code;

    public CommonException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public CommonException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public CommonException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
