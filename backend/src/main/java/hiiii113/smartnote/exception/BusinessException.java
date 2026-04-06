package hiiii113.smartnote.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException
{
    private final String msg; // 返回的错误信息
    private final Integer code; // 返回的状态码

    public BusinessException(String msg, Integer code)
    {
        this.msg = msg;
        this.code = code;
    }
}
