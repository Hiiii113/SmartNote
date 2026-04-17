package hiiii113.smartnote.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig
{
    // 热点笔记缓存：最多 1000 篇，30 分钟未访问过期
    @Bean
    public Caffeine<Object, Object> noteCacheConfig()
    {
        return Caffeine.newBuilder()
                .maximumSize(1000) // 最多 1000 条笔记
                .expireAfterAccess(30, TimeUnit.MINUTES);
    }

    // 最近常看笔记缓存：每个用户最多 3 篇，1 小时过期
    @Bean
    public Caffeine<Object, Object> hotNotesCacheConfig()
    {
        return Caffeine.newBuilder()
                .maximumSize(10000) // 最多 10000 个用户
                .expireAfterWrite(1, TimeUnit.HOURS);
    }

    @Bean
    public CacheManager cacheManager()
    {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache("noteCache", noteCacheConfig().build());
        cacheManager.registerCustomCache("hotNotes", hotNotesCacheConfig().build());
        return cacheManager;
    }
}
