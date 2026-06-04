package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 方案规划子代理的结构化输出结果。
 *
 * @param suggestions 按优先级排序的变更建议列表
 * @param riskLevel   总体风险评估：低 / 中 / 高
 * @param steps       建议的执行步骤列表
 */
public record PlanResult(
        @JsonPropertyDescription("按优先级排序的变更建议列表")
        List<ChangeSuggestion> suggestions,
        @JsonPropertyDescription("总体风险评估：低 / 中 / 高")
        String riskLevel,
        @JsonPropertyDescription("建议的执行步骤列表")
        List<ExecutionStep> steps) {
}
