package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 建议的执行步骤。
 *
 * @param order      执行顺序
 * @param action     操作描述
 * @param reason     执行原因
 * @param isOptional 是否可选
 * @param commands   推荐的命令列表
 */
public record ExecutionStep(
        @JsonPropertyDescription("执行顺序编号")
        int order,
        @JsonPropertyDescription("操作描述")
        String action,
        @JsonPropertyDescription("执行原因")
        String reason,
        @JsonPropertyDescription("是否为可选步骤")
        boolean isOptional,
        @JsonPropertyDescription("推荐的执行命令列表")
        List<String> commands) {
}
