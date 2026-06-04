package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 测试用例信息。
 *
 * @param type            测试类型：UNIT / INTEGRATION / REGRESSION
 * @param name            测试用例名称
 * @param description     测试描述
 * @param expectedResult  预期结果
 */
public record TestCaseInfo(
        @JsonPropertyDescription("测试类型：UNIT / INTEGRATION / REGRESSION")
        String type,
        @JsonPropertyDescription("测试用例名称")
        String name,
        @JsonPropertyDescription("测试描述")
        String description,
        @JsonPropertyDescription("预期结果")
        String expectedResult) {
}
