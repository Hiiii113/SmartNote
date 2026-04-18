package hiiii113.smartnote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI 助手配置属性
 * 和 application.yml 里面的 prompt 绑定，自动获取 prompt
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-assistant") // 自动读入配置，SpringBoot 会把里面的字段 prompts 和这里面的 map 绑定
public class AiAssistantProperties
{
    // 根据模式名绑定 prompts（router chat note-summary note-summary-chat knowledge-search）
    private Map<String, String> prompts;

    // 根据模式获取提示词，默认闲聊
    public String getPrompt(String mode)
    {
        if (prompts == null) return "";
        return prompts.getOrDefault(mode, prompts.getOrDefault("chat", "")); // 没找到就默认使用 chat 的 prompt
    }
}
