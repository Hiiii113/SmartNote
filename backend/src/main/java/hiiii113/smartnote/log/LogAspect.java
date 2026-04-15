package hiiii113.smartnote.log;

import cn.dev33.satoken.stp.StpUtil;
import hiiii113.smartnote.entity.Log;
import hiiii113.smartnote.service.LogHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 日志记录 aop
 */
@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LogAspect
{
    // LogHandler
    private final LogHandler logHandler;

    // 遇到 @LogAnnotation 的时候进行切入
    @Pointcut("@annotation(hiiii113.smartnote.log.LogAnnotation)")
    public void pointcut()
    {
    }

    // 环绕通知
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable
    {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        // 初始化日志实体
        Log logEntity = new Log();
        logEntity.setOperationTime(LocalDateTime.now());

        // 获取客户端IP
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null)
        {
            HttpServletRequest request = requestAttributes.getRequest();
            logEntity.setClientIp(request.getRemoteAddr());
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        logEntity.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

        // 获取 @LogAnnotation 注解信息
        LogAnnotation logAnnotation = method.getAnnotation(LogAnnotation.class);
        logEntity.setModule(logAnnotation.module());
        logEntity.setOperator(logAnnotation.operator());

        // 获取操作用户
        Object loginId = StpUtil.getLoginIdDefaultNull();
        logEntity.setUserNumber(loginId != null ? loginId.toString() : "-");

        Object businessResult;
        try
        {
            // 执行目标业务方法
            businessResult = joinPoint.proceed();
            logEntity.setResult("成功");
        }
        catch (Exception e)
        {
            logEntity.setResult("失败");
            logEntity.setException(e.getMessage());
            throw e;
        }
        finally
        {
            // 计算操作耗时
            logEntity.setCostTime(System.currentTimeMillis() - startTime);

            // 保存日志（异步执行，不影响主业务）
            saveOperationLog(logEntity);
        }

        return businessResult;
    }

    private void saveOperationLog(Log logEntity)
    {
        try
        {
            logHandler.saveOperationLog(logEntity);
        }
        catch (Exception e)
        {
            log.error("记录系统操作日志失败：{}", e.getMessage());
        }
    }
}
