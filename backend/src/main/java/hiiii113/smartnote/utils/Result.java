package hiiii113.smartnote.utils;

import lombok.Data;

/**
 * 前端统一返回值
 *
 * @param <T> 泛型
 */
@Data
public class Result<T>
{
    // 业务状态码常量
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_CREATED = 201;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_NOT_FOUND = 404;
    public static final int CODE_ERROR = 500;

    // 状态码
    private Integer code;
    // 信息
    private String msg;
    // 数据
    private T data;


    // 成功（无数据）
    public static Result<Void> ok()
    {
        return build(CODE_SUCCESS, "操作成功", null);
    }

    // 成功（带消息）
    public static Result<Void> ok(String message)
    {
        return build(CODE_SUCCESS, message, null);
    }

    // 成功（201）
    public static <T> Result<T> created(String message)
    {
        return build(CODE_CREATED, message, null);
    }

    // 成功（带数据）
    public static <T> Result<T> ok(T data)
    {
        return build(CODE_SUCCESS, "操作成功", data);
    }

    // 成功（带消息 + 数据）
    public static <T> Result<T> ok(String message, T data)
    {
        return build(CODE_SUCCESS, message, data);
    }

    // 成功（201）
    public static <T> Result<T> created(String message, T data)
    {
        return build(CODE_CREATED, message, data);
    }

    // 失败（仅消息，默认400）
    public static Result<Void> fail(String message)
    {
        return build(CODE_BAD_REQUEST, message, null);
    }

    // 失败（消息 + 状态码）
    public static Result<Void> fail(String message, int code)
    {
        return build(code, message, null);
    }

    // 构造
    private static <T> Result<T> build(int code, String message, T data)
    {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setData(data);
        return result;
    }
}
