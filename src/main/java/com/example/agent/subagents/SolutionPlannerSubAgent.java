package com.example.agent.subagents;

import com.example.agent.core.SubAgentResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 方案规划子代理（第二阶段）。
 * <p>
 * 基于第一阶段（{@link CodeResearchSubAgent}）的研究结果，
 * 利用大语言模型生成具体的实施建议方案，包括：
 * <ul>
 *   <li><b>修改建议</b> - 按优先级排列的具体改动点</li>
 *   <li><b>涉及文件</b> - 需要修改或新增的文件/模块清单</li>
 *   <li><b>风险评估</b> - 潜在影响面分析与回滚点</li>
 *   <li><b>执行步骤</b> - 建议的实施顺序和操作指南</li>
 * </ul>
 * </p>
 *
 * @author FanMang776
 * @see CodeResearchSubAgent
 * @see TestVerifierSubAgent
 */
@Component
public class SolutionPlannerSubAgent {

    /** Spring AI ChatClient，用于与大语言模型交互 */
    private final ChatClient chatClient;

    /**
     * 构造函数，通过 ChatClient.Builder 构建 ChatClient 实例。
     *
     * @param chatClientBuilder ChatClient 构建器（由 Spring AI 自动注入）
     */
    public SolutionPlannerSubAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 执行方案规划分析。
     * <p>
     * 将用户需求和代码研究结果组合成 Prompt 发送给 LLM，
     * 获取结构化的实施方案建议。
     * </p>
     *
     * @param requirement     用户的原始需求描述
     * @param researchDetails 第一阶段的代码研究结果详情
     * @return 包含方案规划结果的 {@link SubAgentResult}
     */
    public SubAgentResult run(String requirement, String researchDetails) {
        // 构建中文 Prompt，引导 LLM 输出结构化方案
        String prompt = """
                你是方案生成子代理。基于下面信息输出：
                1) 修改建议（按优先级）
                2) 涉及文件/模块
                3) 风险与回滚点
                4) 建议执行步骤

                [用户需求]
                %s

                [代码检索结果]
                %s
                """.formatted(requirement, researchDetails);

        // 调用 LLM 生成方案
        String result = chatClient.prompt(prompt).call().content();

        return new SubAgentResult("SolutionPlanner", "完成方案生成与影响面分析", result);
    }
}
