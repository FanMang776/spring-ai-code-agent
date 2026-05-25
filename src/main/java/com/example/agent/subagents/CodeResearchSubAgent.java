package com.example.agent.subagents;

import com.example.agent.core.SubAgentResult;
import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.GrepTool;
import org.springaicommunity.agent.tools.ListDirectoryTool;
import org.springaicommunity.agent.tools.ShellTools;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.regex.Pattern;

@Component
public class CodeResearchSubAgent {

    private final GrepTool grepTool = GrepTool.builder().build();
    private final FileSystemTools fileSystemTools = FileSystemTools.builder().build();
    private final ListDirectoryTool listDirectoryTool = ListDirectoryTool.builder().build();
    private final ShellTools shellTools = ShellTools.builder().build();

    public SubAgentResult run(String requirement, Path projectPath) {
        String path = projectPath.toAbsolutePath().toString();
        String files = listDirectoryTool.listDirectory(path, 2, 200);
        String grepPattern = Pattern.quote(requirement);
        String grep = grepTool.grep(grepPattern, path, "**/*", GrepTool.OutputMode.content, 50, 2, 300, true, false, "", 10, 200, false);
        String pom = fileSystemTools.read(projectPath.resolve("pom.xml").toString(), 1, 200);
        String rawShell = shellTools.bash("git status --short --branch", 10000L, path, false);
        String shell = normalizeShellStatus(rawShell);

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
