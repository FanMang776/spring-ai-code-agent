package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * 匹配到的代码片段。
 *
 * @param filePath   文件路径
 * @param lineNumber 起始行号
 * @param content    代码内容
 * @param context    前后几行的上下文
 */
public record CodeSnippet(
        @JsonPropertyDescription("文件路径")
        String filePath,
        @JsonPropertyDescription("起始行号")
        int lineNumber,
        @JsonPropertyDescription("匹配到的代码内容")
        String content,
        @JsonPropertyDescription("代码上下文（前后若干行）")
        String context) {
}
