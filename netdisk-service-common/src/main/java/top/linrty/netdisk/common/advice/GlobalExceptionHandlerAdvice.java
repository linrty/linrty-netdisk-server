package top.linrty.netdisk.common.advice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import top.linrty.netdisk.common.domain.dto.RestResult;
import top.linrty.netdisk.common.enums.ResultCodeEnum;
import top.linrty.netdisk.common.exception.*;

/**
 * 该注解为统一异常处理的核心
 *
 * 是一种作用于控制层的切面通知（Advice），该注解能够将通用的@ExceptionHandler、@InitBinder和@ModelAttributes方法收集到一个类型，并应用到所有控制器上
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlerAdvice {

    /**-------- 通用异常处理方法 --------**/
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResult error(Exception e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.fail();    // 通用异常结果
    }

    /**-------- 指定异常处理方法 --------**/
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResult error(NullPointerException e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.setResult(ResultCodeEnum.NULL_POINT);
    }
    /**-------- 下标越界处理方法 --------**/
    @ExceptionHandler(IndexOutOfBoundsException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResult error(IndexOutOfBoundsException e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.setResult(ResultCodeEnum.INDEX_OUT_OF_BOUNDS);
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RestResult error(NotLoginException e) {
        e.printStackTrace();
        log.error("全局异常捕获：" + e);
        return RestResult.setResult(ResultCodeEnum.NOT_LOGIN_ERROR);
    }


    /**
     * 方法参数校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return RestResult.setResult(ResultCodeEnum.PARAM_ERROR).message(e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(UserException.class)
    @ResponseBody
    public RestResult handleUserException(UserException e) {
        log.error(e.getMessage(), e);
        return processResponse(e);
    }

    @ExceptionHandler(DBException.class)
    @ResponseBody
    public RestResult handleDBException(DBException e) {
        log.error(e.getMessage(), e);
        return processResponse(e);
    }

    @ExceptionHandler(StorageException.class)
    @ResponseBody
    public RestResult handleStorageException(StorageException e) {
        log.error(e.getMessage(), e);
        return processResponse(e);
    }

    @ExceptionHandler(FileOperationException.class)
    @ResponseBody
    public RestResult handleFileOperationException(FileOperationException e) {
        log.error(e.getMessage(), e);
        return processResponse(e);
    }



    private RestResult processResponse(CommonException e){
        return RestResult.fail()
                .message(e.getMessage())
                .code(e.getCode());
    }
}