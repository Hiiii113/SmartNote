package hiiii113.smartnote.exception;

import hiiii113.smartnote.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * 全局异常拦截器，统一处理异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    // 业务异常，返回前端
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleServiceException(BusinessException e)
    {
        log.error("业务异常: ", e);
        return Result.fail(e.getMsg(), e.getCode());
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e)
    {
        // 取第一条错误信息返回给前端
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String message = fieldError.getDefaultMessage();
        log.warn("参数校验失败：{}", message);
        return Result.fail(message, Result.CODE_BAD_REQUEST);
    }

    // 数据库异常，返回前端
    @ExceptionHandler(SQLException.class)
    public Result<Void> handleSQLException(SQLException e)
    {
        log.error("数据库异常: ", e);
        return Result.fail("数据库错误！");
    }

    // 未知异常，返回前端
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e)
    {
        log.error("未知异常: ", e);
        return Result.fail("服务器内部错误！");
    }
}
