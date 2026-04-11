package hiiii113.smartnote.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration
{
    @Bean
    public ChatClient chatClient(OpenAiChatModel model)
    {
        return ChatClient.builder(model)
                .defaultSystem("你是一个笔记管理大师，可以分析现有的笔记文章，并总结，给出这篇笔记对应的标签等建议，帮助用户建立一个完善的知识库") // 系统提示词
                .build();
    }
}
