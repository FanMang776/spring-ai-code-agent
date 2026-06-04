package com.example.agent.core;

import com.example.agent.dto.PlanResult;
import com.example.agent.dto.ResearchResult;
import com.example.agent.dto.TestPlanResult;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 会话上下文。
 * <p>
 * 每个会话维护独立的对话记忆、分析结果缓存和目标项目路径。
 * 支持多项目同时分析，会话间数据隔离。
 * </p>
 */
public class SessionContext {

    private final String sessionId;
    private final ChatMemory chatMemory;
    private final Path projectPath;
    private final LocalDateTime createdAt;

    private ResearchResult cachedResearch;
    private PlanResult cachedPlan;
    private TestPlanResult cachedTestPlan;
    private String lastCommand;

    public SessionContext(String sessionId, Path projectPath) {
        this.sessionId = sessionId;
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new org.springframework.ai.chat.memory.InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        this.projectPath = projectPath;
        this.createdAt = LocalDateTime.now();
    }

    public String sessionId() {
        return sessionId;
    }

    public ChatMemory chatMemory() {
        return chatMemory;
    }

    public Path projectPath() {
        return projectPath;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public String lastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String cmd) {
        this.lastCommand = cmd;
    }

    public void cacheResearch(ResearchResult result) {
        this.cachedResearch = result;
    }

    public Optional<ResearchResult> getCachedResearch() {
        return Optional.ofNullable(cachedResearch);
    }

    public void cachePlan(PlanResult result) {
        this.cachedPlan = result;
    }

    public Optional<PlanResult> getCachedPlan() {
        return Optional.ofNullable(cachedPlan);
    }

    public void cacheTestPlan(TestPlanResult result) {
        this.cachedTestPlan = result;
    }

    public Optional<TestPlanResult> getCachedTestPlan() {
        return Optional.ofNullable(cachedTestPlan);
    }
}
