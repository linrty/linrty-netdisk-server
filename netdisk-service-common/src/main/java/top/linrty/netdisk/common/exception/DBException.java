package top.linrty.netdisk.common.exception;

import top.linrty.netdisk.common.enums.ResultCodeEnum;

public class DBException extends CommonException{
    public DBException(String message) {
        super(message, ResultCodeEnum.DB_ERROR.getCode());
    }

    public DBException(String message, Throwable cause) {
        super(message, cause, ResultCodeEnum.DB_ERROR.getCode());
    }

    public DBException(Throwable cause) {
        super(ResultCodeEnum.DB_ERROR.getMessage(), cause, ResultCodeEnum.DB_ERROR.getCode());
    }
}
