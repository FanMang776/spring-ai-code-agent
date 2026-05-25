package com.example.agent.subagents;

import com.example.agent.core.SubAgentResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 测试验证子代理（第三阶段）。
 * <p>
 * 基于用户需求和第二阶段（{@link SolutionPlannerSubAgent}）的方案规划结果，
 * 利用大语言模型生成测试验证建议，包括：
 * <ul>
 *   <li><b>测试清单</b> - 单元测试 / 集成测试 / 回归测试用例</li>
 *   <li><b>执行命令</b> - Maven / Gradle / Shell 测试命令示例</li>
 *   <li><b>验收标准</b> - 功能验收的判定条件</li>
 *   <li><b>风险提示</b> - 未覆盖的测试场景及潜在风险</li>
 * </ul>
 * </p>
 *
 * @author FanMang776
 * @see SolutionPlannerSubAgent
 * @see CodeResearchSubAgent
 */
@Component
public class TestVerifierSubAgent {

    /** Spring AI ChatClient，用于与大语言模型交互 */
    private final ChatClient chatClient;

    /**
     * 构造函数，通过 ChatClient.Builder 构建 ChatClient 实例。
     *
     * @param chatClientBuilder ChatClient 构建器（由 Spring AI 自动注入）
     */
    public TestVerifierSubAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 执行测试验证分析。
     * <p>
     * 将用户需求和方案规划结果组合成 Prompt 发送给 LLM，
     * 获取结构化的测试验证建议。
     * </p>
     *
     * @param requirement 用户的原始需求描述
     * @param planDetails 第二阶段的方案规划结果详情
     * @return 包含测试验证结果的 {@link SubAgentResult}
     */
    public SubAgentResult run(String requirement, String planDetails) {
        // 构建中文 Prompt，引导 LLM 输出结构化测试建议
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

        // 调用 LLM 生成测试建议
        String result = chatClient.prompt(prompt).call().content();

        return new SubAgentResult("TestVerifier", "完成测试验证建议与验收标准定义", result);
    }
}
