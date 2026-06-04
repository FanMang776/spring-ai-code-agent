package com.example.agent.core;

import com.example.agent.dto.PlanResult;
import com.example.agent.dto.ResearchResult;
import com.example.agent.dto.SessionSnapshot;
import com.example.agent.dto.TestPlanResult;
import com.example.agent.subagents.CodeResearchSubAgent;
import com.example.agent.subagents.SolutionPlannerSubAgent;
import com.example.agent.subagents.TestVerifierSubAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话感知的编排服务。
 * <p>
 * 管理多个 {@link SessionContext}，支持多项目并发分析。
 * 每个会话维护独立 ChatMemory 和分析结果缓存。
 * </p>
 */
@Service
public class SessionOrchestrationService {

    private final CodeResearchSubAgent codeResearchSubAgent;
    private final SolutionPlannerSubAgent solutionPlannerSubAgent;
    private final TestVerifierSubAgent testVerifierSubAgent;
    private final ChatClient chatClient;
    private final AgentTodoTracker todoTracker;

    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> sessionHistories = new ConcurrentHashMap<>();

    public SessionOrchestrationService(
            CodeResearchSubAgent codeResearchSubAgent,
            SolutionPlannerSubAgent solutionPlannerSubAgent,
            TestVerifierSubAgent testVerifierSubAgent,
            ChatClient chatClient,
            AgentTodoTracker todoTracker) {
        this.codeResearchSubAgent = codeResearchSubAgent;
        this.solutionPlannerSubAgent = solutionPlannerSubAgent;
        this.testVerifierSubAgent = testVerifierSubAgent;
        this.chatClient = chatClient;
        this.todoTracker = todoTracker;
    }

    /**
     * 创建新会话并执行完整分析。
     */
    public SessionContext analyze(String requirement, Path projectPath) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        SessionContext ctx = new SessionContext(sessionId, projectPath);
        sessions.put(sessionId, ctx);

        todoTracker.init(List.of(
                "Code research and project structure analysis",
                "Solution planning and change suggestions",
                "Test verification and acceptance checklist"
        ));

        todoTracker.markInProgress(0, "Scanning files, searching code, collecting dependency context");
        ResearchResult research = codeResearchSubAgent.run(requirement, projectPath);
        ctx.cacheResearch(research);
        todoTracker.markCompleted(0, "Completed code research");

        todoTracker.markInProgress(1, "Generating implementation suggestions based on findings");
        PlanResult plan = solutionPlannerSubAgent.run(requirement, research);
        ctx.cachePlan(plan);
        todoTracker.markCompleted(1, "Completed solution planning");

        todoTracker.markInProgress(2, "Generating test and validation suggestions");
        TestPlanResult testPlan = testVerifierSubAgent.run(requirement, plan);
        ctx.cacheTestPlan(testPlan);
        todoTracker.markCompleted(2, "Completed verification suggestions");

        ctx.setLastCommand("analyze " + requirement);
        recordHistory(sessionId, "analyze: " + requirement);

        return ctx;
    }

    /**
     * 基于已缓存的会话结果进行追问。
     */
    public String ask(SessionContext ctx, String question) {
        ResearchResult research = ctx.getCachedResearch()
                .orElseThrow(() -> new IllegalStateException("No research data in session. Run analyze first."));

        String prompt = """
                你是代码分析助手。基于之前的分析结果回答用户的问题。

                [分析上下文]
                项目目录: %s
                依赖: %s

                [用户问题]
                %s
                """.formatted(
                research.directoryTree(),
                String.join(", ", research.dependencyInfo().keyDependencies()),
                question);

        String answer = chatClient.prompt(prompt).call().content();
        ctx.setLastCommand("ask: " + question);
        recordHistory(ctx.sessionId(), "ask: " + question);
        return answer;
    }

    /**
     * 获取指定会话。
     */
    public Optional<SessionContext> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 获取所有会话快照。
     */
    public List<SessionSnapshot> listSessions() {
        List<SessionSnapshot> list = new ArrayList<>();
        for (SessionContext ctx : sessions.values()) {
            list.add(new SessionSnapshot(
                    ctx.sessionId(),
                    ctx.projectPath().toString(),
                    ctx.createdAt(),
                    ctx.chatMemory().get(ctx.sessionId()).size(),
                    ctx.lastCommand()));
        }
        return list;
    }

    /**
     * 移除并清理会话。
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        sessionHistories.remove(sessionId);
    }

    /**
     * 获取会话历史记录。
     */
    public List<String> getHistory(String sessionId) {
        return sessionHistories.getOrDefault(sessionId, List.of());
    }

    private void recordHistory(String sessionId, String entry) {
        sessionHistories.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(
                LocalDateTime.now().toLocalTime().toString().substring(0, 8) + " " + entry);
    }
}
