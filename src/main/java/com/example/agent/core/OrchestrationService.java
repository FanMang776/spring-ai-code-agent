package com.example.agent.core;

import com.example.agent.subagents.CodeResearchSubAgent;
import com.example.agent.subagents.SolutionPlannerSubAgent;
import com.example.agent.subagents.TestVerifierSubAgent;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class OrchestrationService {

    private final AgentTodoTracker todoTracker;
    private final CodeResearchSubAgent codeResearchSubAgent;
    private final SolutionPlannerSubAgent solutionPlannerSubAgent;
    private final TestVerifierSubAgent testVerifierSubAgent;

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

    public String analyze(String requirement, Path projectPath) {
        if (!Files.exists(projectPath)) {
            throw new IllegalArgumentException("Project path does not exist: " + projectPath);
        }

        todoTracker.init(List.of(
                "Code research and project structure analysis",
                "Solution planning and change suggestions",
                "Test verification and acceptance checklist",
                "Final report aggregation"
        ));

        todoTracker.markInProgress(0, "Scanning files, searching code, collecting dependency context");
        SubAgentResult research = codeResearchSubAgent.run(requirement, projectPath);
        todoTracker.markCompleted(0, "Completed code research");

        todoTracker.markInProgress(1, "Generating implementation suggestions based on findings");
        SubAgentResult plan = solutionPlannerSubAgent.run(requirement, research.details());
        todoTracker.markCompleted(1, "Completed solution planning");

        todoTracker.markInProgress(2, "Generating test and validation suggestions");
        SubAgentResult verify = testVerifierSubAgent.run(requirement, plan.details());
        todoTracker.markCompleted(2, "Completed verification suggestions");

        todoTracker.markInProgress(3, "Building final report");
        todoTracker.markCompleted(3, "Completed final report");
        return renderReport(requirement, projectPath, research, plan, verify);
    }

    private String renderReport(String requirement, Path projectPath, SubAgentResult research, SubAgentResult plan, SubAgentResult verify) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Implementation Suggestion Report\n\n");
        sb.append("## Input\n");
        sb.append("- Requirement: ").append(requirement).append("\n");
        sb.append("- Project path: ").append(projectPath.toAbsolutePath()).append("\n\n");
        sb.append("- Note: If Chinese comments look garbled in terminal output, verify file encoding in IDE before judging source content.\n\n");

        sb.append("## Todo Summary\n");
        for (var item : todoTracker.snapshot()) {
            sb.append("- ").append(item.status()).append(" | ").append(item.content()).append(" | ").append(item.activeForm()).append("\n");
        }
        sb.append("\n");

        sb.append("## Code Research\n");
        sb.append(research.details()).append("\n\n");

        sb.append("## Solution Suggestions\n");
        sb.append(plan.details()).append("\n\n");

        sb.append("## Test Suggestions\n");
        sb.append(verify.details()).append("\n");
        return sb.toString();
    }
}
