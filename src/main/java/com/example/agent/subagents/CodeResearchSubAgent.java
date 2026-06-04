package com.example.agent.subagents;

import com.example.agent.dto.CodeSnippet;
import com.example.agent.dto.DependencyInfo;
import com.example.agent.dto.ResearchResult;
import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.GrepTool;
import org.springaicommunity.agent.tools.ListDirectoryTool;
import org.springaicommunity.agent.tools.ShellTools;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码研究子代理（第一阶段）。
 * <p>
 * 负责对目标项目进行全面的代码调研和信息收集，返回结构化的 {@link ResearchResult}。
 * </p>
 */
@Component
public class CodeResearchSubAgent {

    private final GrepTool grepTool = GrepTool.builder().build();
    private final FileSystemTools fileSystemTools = FileSystemTools.builder().build();
    private final ListDirectoryTool listDirectoryTool = ListDirectoryTool.builder().build();
    private final ShellTools shellTools = ShellTools.builder().build();

    /**
     * 执行代码研究分析。
     *
     * @param requirement 用户的研发需求描述
     * @param projectPath 目标项目的绝对路径
     * @return 结构化的研究结果 {@link ResearchResult}
     */
    public ResearchResult run(String requirement, Path projectPath) {
        String path = projectPath.toAbsolutePath().toString();

        // 1. 列出项目目录结构
        String files = listDirectoryTool.listDirectory(path, 2, 200);

        // 2. 搜索代码内容
        String grepPattern = Pattern.quote(requirement);
        String grepOutput = grepTool.grep(grepPattern, path, "**/*", GrepTool.OutputMode.content,
                50, 2, 300, true, false, "", 10, 200, false);
        List<CodeSnippet> snippets = parseSnippets(grepOutput);

        // 3. 读取 pom.xml
        String pom = fileSystemTools.read(projectPath.resolve("pom.xml").toString(), 1, 200);
        DependencyInfo depInfo = parseDependencyInfo(pom);

        // 4. Git 状态
        String rawShell = shellTools.bash("git status --short --branch", 10000L, path, false);
        String gitStatus = normalizeShellStatus(rawShell);

        return new ResearchResult(files, snippets, depInfo, gitStatus);
    }

    /** 从 grep 输出解析为 CodeSnippet 列表（格式：path:line:content + 上下文） */
    private List<CodeSnippet> parseSnippets(String grepOutput) {
        if (grepOutput == null || grepOutput.isBlank()) {
            return Collections.emptyList();
        }
        List<CodeSnippet> result = new ArrayList<>();
        String[] lines = grepOutput.split("\n");
        for (String line : lines) {
            Matcher m = Pattern.compile("^([^:]+):(\\d+):(.*)").matcher(line);
            if (m.find()) {
                result.add(new CodeSnippet(
                        m.group(1),
                        Integer.parseInt(m.group(2)),
                        m.group(3),
                        ""));
            }
        }
        return result;
    }

    /** 从 pom.xml 内容片段提取依赖信息 */
    private DependencyInfo parseDependencyInfo(String pom) {
        if (pom == null || pom.isBlank()) {
            return new DependencyInfo("", "", "", Collections.emptyList());
        }
        String groupId = extractXmlTag(pom, "groupId", 0);
        String artifactId = extractXmlTag(pom, "artifactId", 0);
        String javaVersion = extractJavaVersion(pom);
        List<String> deps = extractDependencies(pom);
        return new DependencyInfo(groupId, artifactId, javaVersion, deps);
    }

    private String extractXmlTag(String xml, String tag, int occurrence) {
        Pattern p = Pattern.compile("<" + tag + ">([^<]+)</" + tag + ">");
        Matcher m = p.matcher(xml);
        for (int i = 0; i <= occurrence && m.find(); i++) {
            if (i == occurrence) return m.group(1);
        }
        return "";
    }

    private String extractJavaVersion(String pom) {
        Matcher m = Pattern.compile("<java\\.version>([^<]+)</java\\.version>").matcher(pom);
        return m.find() ? m.group(1) : "17+";
    }

    private List<String> extractDependencies(String pom) {
        List<String> deps = new ArrayList<>();
        // 匹配 <artifactId>xxx</artifactId> 但排除 parent / project 自身的
        Matcher m = Pattern.compile("<artifactId>([^<]+)</artifactId>").matcher(pom);
        while (m.find()) {
            String art = m.group(1);
            if (!art.equals(artifactIdFromPom(pom)) && !art.equals("spring-boot-starter-parent")) {
                deps.add(art);
            }
        }
        return deps.size() > 10 ? deps.subList(0, 10) : deps;
    }

    private String artifactIdFromPom(String pom) {
        return extractXmlTag(pom, "artifactId", 1);
    }

    private String normalizeShellStatus(String raw) {
        String lower = raw == null ? "" : raw.toLowerCase();
        if (lower.contains("not a git repository")) {
            return """
                    [Warning] Current path is not a Git repository.
                    Suggestion: run `git init` in the project root if version control is expected.
                    """;
        }
        return raw != null ? raw : "";
    }
}
