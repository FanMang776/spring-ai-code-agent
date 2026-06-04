package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 代码研究子代理的结构化输出结果。
 *
 * @param directoryTree    项目目录结构树
 * @param matchedSnippets  匹配到的代码片段列表
 * @param dependencyInfo   pom.xml 中的关键依赖信息
 * @param gitStatus        Git 仓库当前状态
 */
public record ResearchResult(
        @JsonPropertyDescription("项目目录结构树")
        String directoryTree,
        @JsonPropertyDescription("匹配到的代码片段列表")
        List<CodeSnippet> matchedSnippets,
        @JsonPropertyDescription("pom.xml 中的关键依赖信息")
        DependencyInfo dependencyInfo,
        @JsonPropertyDescription("Git 仓库当前状态")
        String gitStatus) {
}
