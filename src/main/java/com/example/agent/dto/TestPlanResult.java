package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 测试验证子代理的结构化输出结果。
 *
 * @param testCases          建议的测试用例列表
 * @param suggestedCommands  建议的测试执行命令
 * @param acceptanceCriteria 验收标准
 * @param uncoveredRisks     未覆盖的风险点
 */
public record TestPlanResult(
        @JsonPropertyDescription("建议的测试用例列表")
        List<TestCaseInfo> testCases,
        @JsonPropertyDescription("建议的测试执行命令列表")
        List<String> suggestedCommands,
        @JsonPropertyDescription("验收标准列表")
        List<String> acceptanceCriteria,
        @JsonPropertyDescription("未覆盖的风险点列表")
        List<String> uncoveredRisks) {
}
