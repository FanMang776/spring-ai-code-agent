package com.example.agent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 内容安全过滤 Advisor。
 * <p>检测用户输入中的敏感信息（如密码、密钥等），自动脱敏后再发送给 LLM。</p>
 */
@Component
public class ContentFilterAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(ContentFilterAdvisor.class);

    /** 敏感信息正则：密码、密钥模式 */
    private static final Pattern SENSITIVE_PATTERN =
            Pattern.compile("(密码[是:：]?|password[=:]?|密钥[=:]?|secret[=:]?|api.?key[=:]?|token[=:]?)\\s*([\\w@#$%^&*!]{4,})",
                    Pattern.CASE_INSENSITIVE);

    /** 脱敏替换 */
    private static final String MASK = "****";

    @Override
    public String getName() {
        return "ContentFilterAdvisor";
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String content = request.prompt().getContents();
        String masked = SENSITIVE_PATTERN.matcher(content).replaceAll("$1" + MASK);

        if (!masked.equals(content)) {
            log.info("Sensitive content detected and masked in user input.");
        }

        var mutated = request.mutate()
                .prompt(mutatePrompt(request, masked))
                .build();
        return mutated;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 30;
    }

    /**
     * 替换 Prompt 中的用户消息内容为脱敏后的文本。
     */
    private org.springframework.ai.chat.prompt.Prompt mutatePrompt(
            ChatClientRequest request, String maskedContent) {
        return new org.springframework.ai.chat.prompt.Prompt(maskedContent,
                request.prompt().getOptions());
    }
}
