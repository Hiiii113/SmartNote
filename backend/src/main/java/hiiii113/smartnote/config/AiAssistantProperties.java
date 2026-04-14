package hiiii113.smartnote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI 助手配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-assistant") // 自动读入配置
public class AiAssistantProperties
{
    // 根据模式名绑定 prompt
    private Map<String, String> prompts;

    // 根据模式获取提示词，默认闲聊
    public String getPrompt(String mode)
    {
        if (prompts == null) return "";
        return prompts.getOrDefault(mode, prompts.getOrDefault("chat", ""));
    }
}
