package top.linrty.netdisk.common.exception;

import top.linrty.netdisk.common.enums.ResultCodeEnum;

public class StorageException extends CommonException{
    public StorageException(String message) {
        super(message, ResultCodeEnum.STORAGE_ERROR.getCode());
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause, ResultCodeEnum.STORAGE_ERROR.getCode());
    }

    public StorageException(Throwable cause) {
        super(ResultCodeEnum.STORAGE_ERROR.getMessage(), cause, ResultCodeEnum.STORAGE_ERROR.getCode());
    }
}
