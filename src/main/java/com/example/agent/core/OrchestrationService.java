package com.example.agent.core;

import com.example.agent.subagents.CodeResearchSubAgent;
import com.example.agent.subagents.SolutionPlannerSubAgent;
import com.example.agent.subagents.TestVerifierSubAgent;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 多 Agent 编排服务（核心编排层）。
 * <p>
 * 负责协调整个代码分析流程，按顺序调用三个子代理：
 * <ol>
 *   <li>{@link CodeResearchSubAgent} - 代码研究：分析项目结构、搜索代码内容</li>
 *   <li>{@link SolutionPlannerSubAgent} - 方案规划：基于研究结果生成实施建议</li>
 *   <li>{@link TestVerifierSubAgent} - 测试验证：输出测试策略与验收标准</li>
 * </ol>
 * 最终将所有子代理的结果聚合为 Markdown 格式的实施建议报告。
 * </p>
 *
 * @author FanMang776
 */
@Service
public class OrchestrationService {

    /** 任务进度跟踪器 */
    private final AgentTodoTracker todoTracker;

    /** 代码研究子代理 - 负责项目结构分析和代码搜索 */
    private final CodeResearchSubAgent codeResearchSubAgent;

    /** 方案规划子代理 - 负责生成变更建议和实施方案 */
    private final SolutionPlannerSubAgent solutionPlannerSubAgent;

    /** 测试验证子代理 - 负责制定测试策略和验收标准 */
    private final TestVerifierSubAgent testVerifierSubAgent;

    /**
     * 构造函数，通过依赖注入初始化所有子代理和工具。
     *
     * @param todoTracker             任务进度跟踪器
     * @param codeResearchSubAgent    代码研究子代理
     * @param solutionPlannerSubAgent 方案规划子代理
     * @param testVerifierSubAgent    测试验证子代理
     */
    public OrchestrationService(
            AgentTodoTracker todoTracker,
            CodeResearchSubAgent codeResearchSubAgent,
            SolutionPlannerSubAgent solutionPlannerSubAgent,
            TestVerifierSubAgent testVerifierSubAgent
    ) {
        this.todoTracker = todoTracker;
        this.codeResearchSubAgent = codeResearchSubAgent;
        this.solutionPlannerSubAgent = solutionPlannerSubAgent;
        this.testVerifierSubAgent = testVerifierSubAgent;
    }

    /**
     * 执行完整的分析流程。
     * <p>
     * 流程步骤：
     * <ol>
     *   <li>校验项目路径是否存在</li>
     *   <li>初始化任务进度列表</li>
     *   <li>按顺序执行三个子代理</li>
     *   <li>聚合结果并生成 Markdown 报告</li>
     * </ol>
     * </p>
     *
     * @param requirement 用户的研发需求描述
     * @param projectPath 目标项目的绝对路径
     * @return Markdown 格式的实施建议报告
     * @throws IllegalArgumentException 当项目路径不存在时抛出
     */
    public String analyze(String requirement, Path projectPath) {
        // 校验目标路径是否存在
        if (!Files.exists(projectPath)) {
            throw new IllegalArgumentException("Project path does not exist: " + projectPath);
        }

        // 初始化 Todo 任务列表
        todoTracker.init(List.of(
                "Code research and project structure analysis",
                "Solution planning and change suggestions",
                "Test verification and acceptance checklist",
                "Final report aggregation"
        ));

        // 第一步：代码研究 - 扫描文件、搜索代码、收集依赖上下文
        todoTracker.markInProgress(0, "Scanning files, searching code, collecting dependency context");
        SubAgentResult research = codeResearchSubAgent.run(requirement, projectPath);
        todoTracker.markCompleted(0, "Completed code research");

        // 第二步：方案规划 - 基于研究结果生成实施建议
        todoTracker.markInProgress(1, "Generating implementation suggestions based on findings");
        SubAgentResult plan = solutionPlannerSubAgent.run(requirement, research.details());
        todoTracker.markCompleted(1, "Completed solution planning");

        // 第三步：测试验证 - 生成测试和验证建议
        todoTracker.markInProgress(2, "Generating test and validation suggestions");
        SubAgentResult verify = testVerifierSubAgent.run(requirement, plan.details());
        todoTracker.markCompleted(2, "Completed verification suggestions");

        // 第四步：生成最终报告
        todoTracker.markInProgress(3, "Building final report");
        todoTracker.markCompleted(3, "Completed final report");

        return renderReport(requirement, projectPath, research, plan, verify);
    }

    /**
     * 将各阶段的分析结果渲染为 Markdown 格式的最终报告。
     *
     * @param requirement 用户需求描述
     * @param projectPath 目标项目路径
     * @param research    代码研究结果
     * @param plan        方案规划结果
     * @param verify      测试验证结果
     * @return 完整的 Markdown 格式报告字符串
     */
    private String renderReport(String requirement, Path projectPath, SubAgentResult research, SubAgentResult plan, SubAgentResult verify) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Implementation Suggestion Report\n\n");

        // 输入信息摘要
        sb.append("## Input\n");
        sb.append("- Requirement: ").append(requirement).append("\n");
        sb.append("- Project path: ").append(projectPath.toAbsolutePath()).append("\n\n");
        sb.append("- Note: If Chinese comments look garbled in terminal output, verify file encoding in IDE before judging source content.\n\n");

        // Todo 进度状态
        sb.append("## Todo Summary\n");
        for (var item : todoTracker.snapshot()) {
            sb.append("- ").append(item.status()).append(" | ").append(item.content()).append(" | ").append(item.activeForm()).append("\n");
        }
        sb.append("\n");

        // 各阶段分析结果
        sb.append("## Code Research\n");
        sb.append(research.details()).append("\n\n");

        sb.append("## Solution Suggestions\n");
        sb.append(plan.details()).append("\n\n");

        sb.append("## Test Suggestions\n");
        sb.append(verify.details()).append("\n");

        return sb.toString();
    }
}
