package top.linrty.netdisk.common.exception;

import top.linrty.netdisk.common.enums.ResultCodeEnum;

public class FileOperationException extends CommonException{
    public FileOperationException(String message) {
        super(message, ResultCodeEnum.FILE_OPERATION_ERROR.getCode());
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause, ResultCodeEnum.FILE_OPERATION_ERROR.getCode());
    }

    public FileOperationException(Throwable cause) {
        super(ResultCodeEnum.FILE_OPERATION_ERROR.getMessage(), cause, ResultCodeEnum.FILE_OPERATION_ERROR.getCode());
    }
}
