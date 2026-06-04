package com.example.agent.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * 项目依赖信息。
 *
 * @param groupId          项目 groupId
 * @param artifactId       项目 artifactId
 * @param javaVersion      Java 版本
 * @param keyDependencies  核心依赖列表
 */
public record DependencyInfo(
        @JsonPropertyDescription("项目 groupId")
        String groupId,
        @JsonPropertyDescription("项目 artifactId")
        String artifactId,
        @JsonPropertyDescription("Java 版本号")
        String javaVersion,
        @JsonPropertyDescription("核心依赖列表")
        List<String> keyDependencies) {
}
