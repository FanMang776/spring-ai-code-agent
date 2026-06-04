package com.example.agent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 日志记录 Advisor。
 * <p>在请求前注入 traceId 用于链路追踪，响应后记录调用摘要。</p>
 */
@Component
public class LoggingAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        var mutated = request.mutate()
                .context("traceId", traceId)
                .build();
        log.debug("[{}] Request: {}", traceId, request.prompt().getContents());
        return mutated;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        String traceId = (String) response.context().get("traceId");
        var chatResponse = response.chatResponse();

        // 从 ChatResponseMetadata 获取 token 用量（标准 API）
        var metadata = chatResponse.getMetadata();
        Usage usage = metadata.getUsage();
        if (usage != null) {
            log.info("[{}] Token usage: prompt={}, completion={}, total={}",
                    traceId, usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        } else {
            log.info("[{}] No token usage available.", traceId);
        }

        log.debug("[{}] Response received.", traceId);
        return response;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
