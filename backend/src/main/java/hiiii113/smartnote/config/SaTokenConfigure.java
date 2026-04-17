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
        // 验证登录
        registry.addInterceptor(new SaInterceptor(handle ->
                        SaRouter.match("/**")
                                .notMatch("/users/login", "/users/logout", "/upload/**")
                                .notMatch("/users")  // 注册接口 POST /users
                                .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
