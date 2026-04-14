package hiiii113.smartnote.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer
{
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        // 第一个拦截器：初始化 Sa-Token 上下文
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**");

        // 第二个拦截器：验证登录（排除 SSE 接口）
        registry.addInterceptor(new SaInterceptor(handle ->
                        SaRouter.match("/**")
                                .notMatch("/users/login", "/users/logout", "/upload/**")
                                .notMatch("POST /users")
                                .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**")
                .excludePathPatterns("/ai/chat/stream");
    }
}
