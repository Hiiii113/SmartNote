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
        // 注册一个自定义拦截器
        registry.addInterceptor(new SaInterceptor(handle ->
                        SaRouter.match("/**")
                                .notMatch("/users/login", "/users/logout", "/upload/**") // 登录、登出、查看图片放行
                                .notMatch("/users")  // 注册接口 /users 排除
                                .check(r -> StpUtil.checkLogin()))) // 检查是否登录，未登录抛出异常并接住
                .addPathPatterns("/**"); // 对所有路径生效
    }
}
