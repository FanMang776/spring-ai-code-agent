package com.example.agent.subagents;

import com.example.agent.core.SubAgentResult;
import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.GrepTool;
import org.springaicommunity.agent.tools.ListDirectoryTool;
import org.springaicommunity.agent.tools.ShellTools;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * 代码研究子代理（第一阶段）。
 * <p>
 * 负责对目标项目进行全面的代码调研和信息收集，包括：
 * <ul>
 *   <li><b>目录结构</b> - 列出项目文件树（深度限制 2 层）</li>
 *   <li><b>内容搜索</b> - 根据需求关键词正则匹配相关代码</li>
 *   <li><b>依赖分析</b> - 读取 pom.xml 了解技术栈</li>
 *   <li><b>Git 状态</b> - 检查版本控制状态</li>
 * </ul>
 * </p>
 * <p>
 * 本阶段是后续方案规划和测试验证的基础数据来源。
 * </p>
 *
 * @author FanMang776
 * @see SolutionPlannerSubAgent
 * @see TestVerifierSubAgent
 */
@Component
public class CodeResearchSubAgent {

    /** 代码内容搜索工具（基于 ripgrep）*/
    private final GrepTool grepTool = GrepTool.builder().build();

    /** 文件读写工具 */
    private final FileSystemTools fileSystemTools = FileSystemTools.builder().build();

    /** 目录列出工具 */
    private final ListDirectoryTool listDirectoryTool = ListDirectoryTool.builder().build();

    /** Shell 命令执行工具 */
    private final ShellTools shellTools = ShellTools.builder().build();

    /**
     * 执行代码研究分析。
     * <p>
     * 收集以下信息并组装成结构化的研究报告：
     * <ol>
     *   <li>项目目录结构（最多 200 个文件，递归深度 2）</li>
     *   <li>与需求相关的代码片段（最大返回 50 条匹配）</li>
     *   <li>pom.xml 内容片段（了解依赖和技术栈）</li>
     *   <li>Git 仓库状态（分支名、变更文件等）</li>
     * </ol>
     * </p>
     *
     * @param requirement 用户的研发需求描述
     * @param projectPath 目标项目的绝对路径
     * @return 包含完整研究报告的 {@link SubAgentResult}
     */
    public SubAgentResult run(String requirement, Path projectPath) {
        String path = projectPath.toAbsolutePath().toString();

        // 1. 列出项目目录结构（深度 2，上限 200 文件）
        String files = listDirectoryTool.listDirectory(path, 2, 200);

        // 2. 使用正则搜索与需求相关的代码内容
        String grepPattern = Pattern.quote(requirement); // 对特殊字符转义
        String grep = grepTool.grep(grepPattern, path, "**/*", GrepTool.OutputMode.content, 50, 2, 300, true, false, "", 10, 200, false);

        // 3. 读取 pom.xml 了解项目依赖
        String pom = fileSystemTools.read(projectPath.resolve("pom.xml").toString(), 1, 200);

        // 4. 获取 Git 版本控制状态
        String rawShell = shellTools.bash("git status --short --branch", 10000L, path, false);
        String shell = normalizeShellStatus(rawShell);

        // 组装详细报告
        String details = """
                [Directory]
                %s

                [Search]
                %s

                [pom.xml excerpt]
                %s

                [Git status]
                %s
                """.formatted(files, grep, pom, shell);

        return new SubAgentResult("CodeResearch", "Completed repository scanning and context collection", details);
    }

    /**
     * 规范化 Shell 命令的输出结果。
     * <p>
     * 处理非 Git 仓库的特殊情况，给出友好的提示信息。
     * </p>
     *
     * @param raw Shell 命令原始输出
     * @return 规格化后的状态文本
     */
    private String normalizeShellStatus(String raw) {
        String lower = raw == null ? "" : raw.toLowerCase();
        if (lower.contains("not a git repository")) {
            return """
                    [Warning] Current path is not a Git repository.
                    Suggestion: run `git init` in the project root if version control is expected.
                    """;
        }
        return raw;
    }
}
