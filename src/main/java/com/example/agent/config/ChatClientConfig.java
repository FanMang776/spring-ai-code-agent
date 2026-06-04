package com.example.agent.config;

import com.example.agent.advisor.ContentFilterAdvisor;
import com.example.agent.advisor.LoggingAdvisor;
import com.example.agent.advisor.RetryAdvisor;
import com.example.agent.advisor.ValidationAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 ChatClient 配置。
 * <p>
 * 统一构建 ChatClient Bean，注入所有 Advisor 切面组件，
 * 确保所有子代理共享同一个 ChatClient 实例，Advisor 链全局生效。
 * </p>
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                  LoggingAdvisor loggingAdvisor,
                                  RetryAdvisor retryAdvisor,
                                  ContentFilterAdvisor contentFilterAdvisor,
                                  ValidationAdvisor validationAdvisor) {
        return builder
                .defaultAdvisors(
                        loggingAdvisor,
                        retryAdvisor,
                        contentFilterAdvisor,
                        validationAdvisor)
                .build();
    }
}
