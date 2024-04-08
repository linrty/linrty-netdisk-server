package top.linrty.netdisk.common.exception;

import top.linrty.netdisk.common.enums.ResultCodeEnum;

public class UserException extends CommonException{
    public UserException(String message) {
        super(message, ResultCodeEnum.USER_ERROR.getCode());
    }

    public UserException(String message, Throwable cause) {
        super(message, cause, ResultCodeEnum.USER_ERROR.getCode());
    }

    public UserException(Throwable cause) {
        super(ResultCodeEnum.USER_ERROR.getMessage(), cause, ResultCodeEnum.USER_ERROR.getCode());
    }
}
