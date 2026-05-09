package com.wjh.heimaoai.config;

import com.wjh.heimaoai.constants.SystemConstants;
import com.wjh.heimaoai.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import redis.clients.jedis.JedisPooled;

import java.util.List;

@Configuration
public class CommonConfiguration {

    @Bean
    public ChatMemory chatMemory(){
        // 返回一个基于内存的 ChatMemory，使用 MessageWindowChatMemory 进行滑动窗口管理
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(100) // 根据需求设置最大保留的历史消息条数，防止Token超出限制
                .build();
    }

//    @Bean
//    @Primary
//    @ConditionalOnMissingBean(name = "redisVectorStore")
//    public VectorStore redisVectorStore(OpenAiEmbeddingModel openAiEmbeddingModel, JedisPooled jedisPooled) {
//        // 定义可过滤的元数据字段
//        List<RedisVectorStore.MetadataField> metadataFields = List.of(
//                RedisVectorStore.MetadataField.tag("fileName")    // TAG 类型，适合精确匹配或多值
//        );
//        // 使用 Redis 作为向量数据库，用于存储向量数据
//        return RedisVectorStore.builder(jedisPooled, openAiEmbeddingModel)
//                .initializeSchema(true)
//                .indexName("spring_ai_index")
//                .prefix("doc:")
//                .metadataFields(metadataFields)
//                .build();
//    }

//    @Bean
//    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
//        ChatClient build = ChatClient.builder(chatModel)
//                .defaultSystem("你是一个热心、可爱的只能助手，你的名字叫小团团，请以小团团的身份和语气回答问题")
//                .defaultAdvisors(
//                        new SimpleLoggerAdvisor(),
//                        MessageChatMemoryAdvisor.builder(chatMemory).build() // 添加记忆顾问，使模型能够进行多轮对话
//                )
//                .build();
//        return build;
//    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        ChatClient build = ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model("qwen-omni-turbo").build())
                .defaultSystem("你是一个热心、可爱的只能助手，你的名字叫小团团，请以小团团的身份和语气回答问题")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // 添加记忆顾问，使模型能够进行多轮对话
                )
                .build();
        return build;
    }

    @Bean
    public ChatClient gameChatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        ChatClient build = ChatClient.builder(chatModel)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // 添加记忆顾问，使模型能够进行多轮对话
                )
                .build();
        return build;
    }

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel chatModel, ChatMemory chatMemory, CourseTools courseTools) {
        ChatClient build = ChatClient.builder(chatModel)
                .defaultSystem(SystemConstants.SERVICE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // 添加记忆顾问，使模型能够进行多轮对话
                )
                .defaultTools(courseTools)
                .build();
        return build;
    }

    @Bean
    public ChatClient pdfChatClient(OpenAiChatModel chatModel, ChatMemory chatMemory,RedisVectorStore redisVectorStore) {
        ChatClient build = ChatClient.builder(chatModel)
                .defaultSystem("请根据上下文回答问题，遇到上下文没有的问题，回答：不知道")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(redisVectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.6)
                                        .topK(2)
                                        .build())
                                .build()
                )
                .build();
        return build;
    }

}
