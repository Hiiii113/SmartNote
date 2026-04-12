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
    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle ->
        {
            // 除了排除路径，其他路径必须登录
            SaRouter.match("/**")
                    .notMatch("/users/login", "/users/logout", "/upload/**") // 放行注册、登录和登出及静态资源获取接口
                    .notMatch("POST /users")
                    .check(r -> StpUtil.checkLogin()); // 其他检查是否登录

        })).addPathPatterns("/**"); // 所有请求都拦截
    }
}
