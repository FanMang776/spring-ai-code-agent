package com.example.agent.subagents;

import com.example.agent.core.SubAgentResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class TestVerifierSubAgent {

    private final ChatClient chatClient;

    public TestVerifierSubAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public SubAgentResult run(String requirement, String planDetails) {
        String prompt = """
                你是测试验证子代理。基于需求和方案输出：
                1) 建议测试清单（单测/集成/回归）
                2) 建议命令（maven/gradle/shell）
                3) 验收标准
                4) 未覆盖风险

                [用户需求]
                %s

                [方案信息]
                %s
                """.formatted(requirement, planDetails);
        String result = chatClient.prompt(prompt).call().content();
        return new SubAgentResult("TestVerifier", "完成测试验证建议与验收标准定义", result);
    }
}
