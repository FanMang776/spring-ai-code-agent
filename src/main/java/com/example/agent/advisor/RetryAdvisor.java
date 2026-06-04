package com.example.agent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 自动重试 Advisor。
 * <p>当 LLM 调用抛出异常时，以指数退避策略自动重试，最多 3 次。</p>
 */
@Component
public class RetryAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(RetryAdvisor.class);

    /** 最大重试次数 */
    static final int MAX_RETRIES = 3;

    /** 初始等待时间（毫秒） */
    static final long BASE_DELAY_MS = 1000L;

    @Override
    public String getName() {
        return "RetryAdvisor";
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return chain.nextCall(request);
            } catch (RuntimeException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    long delay = BASE_DELAY_MS * (1L << (attempt - 1)); // 指数退避
                    log.warn("LLM call failed (attempt {}/{}), retrying after {}ms: {}",
                            attempt, MAX_RETRIES, delay, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        log.error("LLM call failed after {} attempts", MAX_RETRIES);
        throw lastException;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 50;
    }
}
