package hiiii113.smartnote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class Log
{
    // 日志 id
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 操作用户
    private String userNumber;

    // 操作时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    // 操作模块
    private String module;

    // 操作名
    private String operator;

    // 操作方法全路径
    private String method;

    // 方法参数(JSON格式)
    private String params;

    // 操作结果(JSON格式)
    private String result;

    // 异常信息(失败时记录)
    private String exception;

    // 操作耗时
    private Long costTime;

    // 客户端IP
    private String clientIp;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
