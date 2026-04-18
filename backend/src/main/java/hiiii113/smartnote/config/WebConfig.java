package hiiii113.smartnote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 静态资源映射
 * 把本地图片保存路径映射到 /upload 路径
 */
@Configuration
public class WebConfig implements WebMvcConfigurer
{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        // 项目根目录下的 /upload 路径
        String path = System.getProperty("user.dir") + "/upload/";

        // 映射
        registry.addResourceHandler("/upload/**") // 所有以 /upload 开头的路径都会被这个处理
                .addResourceLocations("file:" + path); // 指示文件的路径
    }
}

