package com.example.agent.subagents;

import com.example.agent.dto.PlanResult;
import com.example.agent.dto.TestPlanResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 测试验证子代理（第三阶段）。
 * <p>
 * 基于 {@link SolutionPlannerSubAgent} 的方案规划结果，
 * 通过 ChatClient 的 {@code .entity(TestPlanResult.class)} 方法获取结构化测试建议。
 * </p>
 */
@Component
public class TestVerifierSubAgent {

    private final ChatClient chatClient;

    public TestVerifierSubAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 执行测试验证分析。
     *
     * @param requirement 用户的原始需求描述
     * @param plan        第二阶段的方案规划结果
     * @return 结构化的测试验证结果 {@link TestPlanResult}
     */
    public TestPlanResult run(String requirement, PlanResult plan) {
        StringBuilder suggestions = new StringBuilder();
        for (var s : plan.suggestions()) {
            suggestions.append("- [P").append(s.priority()).append("] ")
                    .append(s.title()).append(": ").append(s.description()).append("\n");
        }

        String prompt = """
                你是测试验证子代理。基于需求和实施计划，生成测试验证方案。

                [用户需求]
                %s

                [实施计划]
                风险评估: %s
                变更建议:
                %s

                执行步骤:
                %s

                请输出结构化的测试验证方案，包含：
                - testCases: 测试用例列表，每项包含 type(UNIT/INTEGRATION/REGRESSION), name, description, expectedResult
                - suggestedCommands: 建议的测试命令列表
                - acceptanceCriteria: 验收标准列表
                - uncoveredRisks: 未覆盖风险点列表
                """.formatted(
                requirement,
                plan.riskLevel(),
                suggestions.toString(),
                formatSteps(plan));

        return chatClient.prompt(prompt).call().entity(TestPlanResult.class);
    }

    private String formatSteps(PlanResult plan) {
        if (plan.steps() == null || plan.steps().isEmpty()) {
            return "(无明确步骤)";
        }
        StringBuilder sb = new StringBuilder();
        for (var step : plan.steps()) {
            sb.append(step.order()).append(". ").append(step.action())
                    .append(" [").append(step.isOptional() ? "可选" : "必需").append("]")
                    .append(" - ").append(step.reason()).append("\n");
        }
        return sb.toString();
    }
}
