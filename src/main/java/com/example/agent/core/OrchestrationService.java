package com.example.agent.core;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

/**
 * 一次性分析编排服务（兼容模式）。
 * <p>
 * 封装 {@link SessionOrchestrationService} 的一次性分析接口，
 * 供 {@link com.example.agent.cli.AnalyzeCommand} 等兼容场景使用。
 * 新功能推荐直接使用 {@code SessionOrchestrationService} 的交互模式。
 * </p>
 */
@Service
public class OrchestrationService {

    private final SessionOrchestrationService sessionOrchestrationService;

    public OrchestrationService(SessionOrchestrationService sessionOrchestrationService) {
        this.sessionOrchestrationService = sessionOrchestrationService;
    }

    /**
     * 执行单次分析并返回 Markdown 报告。
     *
     * @param requirement 用户需求
     * @param projectPath 目标项目路径
     * @return Markdown 格式的分析报告
     */
    public String analyze(String requirement, Path projectPath) {
        var ctx = sessionOrchestrationService.analyze(requirement, projectPath);
        var research = ctx.getCachedResearch().orElseThrow();
        var plan = ctx.getCachedPlan().orElseThrow();
        var testPlan = ctx.getCachedTestPlan().orElseThrow();

        return renderReport(requirement, projectPath, research, plan, testPlan);
    }

    private String renderReport(String requirement, Path projectPath,
                                 com.example.agent.dto.ResearchResult research,
                                 com.example.agent.dto.PlanResult plan,
                                 com.example.agent.dto.TestPlanResult testPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Implementation Suggestion Report\n\n");
        sb.append("## Input\n");
        sb.append("- Requirement: ").append(requirement).append("\n");
        sb.append("- Project path: ").append(projectPath.toAbsolutePath()).append("\n\n");

        sb.append("## Project Overview\n");
        sb.append("- **Directory**: ").append(research.directoryTree()).append("\n");
        sb.append("- **Dependencies**: ")
                .append(String.join(", ", research.dependencyInfo().keyDependencies())).append("\n");
        sb.append("- **Git Status**: ").append(research.gitStatus()).append("\n\n");

        sb.append("## Solution Suggestions\n");
        if (plan.suggestions() != null) {
            for (var s : plan.suggestions()) {
                sb.append("### [P").append(s.priority()).append("] ").append(s.title()).append("\n");
                sb.append(s.description()).append("\n");
                sb.append("- **Impact**: ").append(s.impactScope()).append("\n");
                if (s.affectedFiles() != null && !s.affectedFiles().isEmpty()) {
                    sb.append("- **Files**: ").append(String.join(", ", s.affectedFiles())).append("\n");
                }
                sb.append("\n");
            }
        }
        sb.append("**Risk Level**: ").append(plan.riskLevel()).append("\n\n");

        sb.append("## Execution Steps\n");
        if (plan.steps() != null) {
            for (var step : plan.steps()) {
                sb.append(step.order()).append(". ").append(step.action()).append("\n");
                if (step.commands() != null && !step.commands().isEmpty()) {
                    sb.append("   ```bash\n");
                    for (String cmd : step.commands()) {
                        sb.append("   ").append(cmd).append("\n");
                    }
                    sb.append("   ```\n");
                }
                sb.append("\n");
            }
        }

        sb.append("## Test Plan\n");
        if (testPlan.testCases() != null) {
            for (var tc : testPlan.testCases()) {
                sb.append("- [").append(tc.type()).append("] ").append(tc.name()).append("\n");
            }
        }
        sb.append("\n**Acceptance Criteria**:\n");
        if (testPlan.acceptanceCriteria() != null) {
            for (var ac : testPlan.acceptanceCriteria()) {
                sb.append("- ").append(ac).append("\n");
            }
        }
        sb.append("\n**Uncovered Risks**:\n");
        if (testPlan.uncoveredRisks() != null) {
            for (var risk : testPlan.uncoveredRisks()) {
                sb.append("- ").append(risk).append("\n");
            }
        }

        return sb.toString();
    }
}
