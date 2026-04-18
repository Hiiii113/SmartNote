package hiiii113.smartnote.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * vectorstore 向量数据库配置类
 */
@Configuration
public class VectorConfig
{
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel)
    {
        // 使用 embeddingModel 返回向量
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 把向量数据库内容存在 JSON 文件中，启动自动加载
        File vectorFile = new File(System.getProperty("user.dir"), "vectors.json");
        if (vectorFile.exists())
        {
            vectorStore.load(vectorFile);
        }

        return vectorStore;
    }
}