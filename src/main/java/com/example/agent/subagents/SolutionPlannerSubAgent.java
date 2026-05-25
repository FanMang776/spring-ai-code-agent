package com.example.agent.subagents;

import com.example.agent.core.SubAgentResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SolutionPlannerSubAgent {

    private final ChatClient chatClient;

    public SolutionPlannerSubAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public SubAgentResult run(String requirement, String researchDetails) {
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
        String result = chatClient.prompt(prompt).call().content();
        return new SubAgentResult("SolutionPlanner", "完成方案生成与影响面分析", result);
    }
}
