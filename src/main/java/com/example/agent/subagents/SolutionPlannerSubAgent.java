package com.example.agent.subagents;

import com.example.agent.dto.PlanResult;
import com.example.agent.dto.ResearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 方案规划子代理（第二阶段）。
 * <p>
 * 基于 {@link CodeResearchSubAgent} 的研究结果，
 * 通过 ChatClient 的 {@code .entity(PlanResult.class)} 方法获取结构化方案输出。
 * </p>
 */
@Component
public class SolutionPlannerSubAgent {

    private final ChatClient chatClient;

    public SolutionPlannerSubAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 执行方案规划分析。
     *
     * @param requirement 用户的原始需求描述
     * @param research    第一阶段的代码研究结果
     * @return 结构化的方案规划结果 {@link PlanResult}
     */
    public PlanResult run(String requirement, ResearchResult research) {
        String prompt = """
                你是方案生成子代理。基于以下项目研究信息，生成实施变更方案。

                [用户需求]
                %s

                [项目目录结构]
                %s

                [依赖信息]
                groupId: %s
                artifactId: %s
                javaVersion: %s
                关键依赖: %s

                [Git状态]
                %s

                请输出结构化的方案，包含以下字段：
                - suggestions: 变更建议列表，每项包含 priority(int), title, description, affectedFiles, impactScope
                - riskLevel: 风险评估（低/中/高）
                - steps: 执行步骤列表，每项包含 order(int), action, reason, isOptional, commands
                """.formatted(
                requirement,
                research.directoryTree(),
                research.dependencyInfo().groupId(),
                research.dependencyInfo().artifactId(),
                research.dependencyInfo().javaVersion(),
                String.join(", ", research.dependencyInfo().keyDependencies()),
                research.gitStatus());

        return chatClient.prompt(prompt).call().entity(PlanResult.class);
    }
}
