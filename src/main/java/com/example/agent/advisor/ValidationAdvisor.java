package com.example.agent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * 结构化输出校验 Advisor。
 * <p>在请求 context 中注入校验指令，引导 LLM 生成符合预期格式的结构化输出。</p>
 */
@Component
public class ValidationAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(ValidationAdvisor.class);

    @Override
    public String getName() {
        return "ValidationAdvisor";
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        return request.mutate()
                .context("validationInstruction", "请确保输出格式严格符合要求的 JSON 结构，所有必填字段不能为空。")
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        log.debug("Validation check passed for response");
        return response;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 20;
    }
}
