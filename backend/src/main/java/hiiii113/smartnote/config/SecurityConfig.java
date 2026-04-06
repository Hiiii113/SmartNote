package hiiii113.smartnote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域问题
 */
@Configuration
public class SecurityConfig
{
    // CORS 配置
    @Bean
    public CorsFilter corsFilter()
    {
        // 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOriginPattern("*");       // 允许所有域名
        config.setAllowCredentials(true);          // 允许携带 cookie
        config.addAllowedHeader("*");              // 允许所有请求头
        config.addAllowedMethod("*");              // 允许所有请求方法

        // 将配置关联到对应的 URL 模式
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // "/**" 表示对所有的路径起效

        // 返回一个 CorsFilter 对象
        return new CorsFilter(source);
    }
}
