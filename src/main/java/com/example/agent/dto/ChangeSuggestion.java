package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 单条变更建议。
 *
 * @param priority      优先级（1 最高）
 * @param title         建议标题
 * @param description   详细描述
 * @param affectedFiles 受影响的文件列表
 * @param impactScope   影响范围描述
 */
public record ChangeSuggestion(
        @JsonPropertyDescription("优先级，1为最高")
        int priority,
        @JsonPropertyDescription("建议标题")
        String title,
        @JsonPropertyDescription("建议详细描述")
        String description,
        @JsonPropertyDescription("受影响文件路径列表")
        List<String> affectedFiles,
        @JsonPropertyDescription("影响范围描述")
        String impactScope) {
}
