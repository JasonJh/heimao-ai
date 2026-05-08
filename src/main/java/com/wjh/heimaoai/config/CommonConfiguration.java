package com.wjh.heimaoai.config;

import com.wjh.heimaoai.constants.SystemConstants;
import com.wjh.heimaoai.tools.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        ChatClient build = ChatClient.builder(chatModel)
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
}
