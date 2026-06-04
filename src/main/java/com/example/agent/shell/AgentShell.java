package com.example.agent.shell;

import com.example.agent.core.SessionContext;
import com.example.agent.core.SessionOrchestrationService;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * REPL Shell 主循环。
 * <p>
 * 使用 JLine3 实现交互式命令行界面，支持命令历史、格式输出。
 * 通过 {@link SessionOrchestrationService} 委派所有分析逻辑。
 * </p>
 */
@Component
public class AgentShell {

    private static final String PROMPT = "agent> ";

    private final SessionOrchestrationService orchestrationService;
    private final LineReader lineReader;

    /** 当前活跃会话 ID */
    private String currentSessionId;

    public AgentShell(SessionOrchestrationService orchestrationService,
                      LineReader lineReader) {
        this.orchestrationService = orchestrationService;
        this.lineReader = lineReader;
    }

    /**
     * 启动 REPL 主循环。
     * 直到用户输入 exit 为止持续读取用户输入并分发命令。
     */
    public void start() {
        ShellPrinter.printBanner();
        ShellPrinter.printHelp();

        while (true) {
            try {
                String line = lineReader.readLine(PROMPT);
                ShellCommand cmd = ShellCommand.parse(line);
                if (!processCommand(cmd)) {
                    break; // exit
                }
            } catch (UserInterruptException e) {
                System.out.println("\n(Type 'exit' to quit)");
            } catch (EndOfFileException e) {
                System.out.println("\nGoodbye!");
                break;
            }
        }
    }

    /**
     * 处理单条命令。
     *
     * @param cmd 解析后的命令
     * @return true 继续循环，false 退出
     */
    private boolean processCommand(ShellCommand cmd) {
        try {
            return switch (cmd.type()) {
                case ANALYZE -> executeAnalyze(cmd);
                case ASK -> executeAsk(cmd);
                case HISTORY -> executeHistory();
                case SESSIONS -> executeSessions();
                case SWITCH -> executeSwitch(cmd);
                case RETRY -> executeRetry(cmd);
                case HELP -> executeHelp();
                case EXIT -> executeExit();
            };
        } catch (Exception e) {
            ShellPrinter.printError(e.getMessage());
            return true;
        }
    }

    private boolean executeAnalyze(ShellCommand cmd) {
        String requirement = cmd.argument();
        var path = cmd.path() != null ? cmd.path().toAbsolutePath().normalize() : java.nio.file.Paths.get("").toAbsolutePath();

        ShellPrinter.print("Analyzing project at: " + path);
        ShellPrinter.print("Requirement: " + requirement);

        SessionContext ctx = orchestrationService.analyze(requirement, path);
        currentSessionId = ctx.sessionId();
        ShellPrinter.printSeparator();
        ShellPrinter.print("Session: " + ctx.sessionId());
        printCachedResults(ctx);
        ShellPrinter.printSeparator();
        return true;
    }

    private boolean executeAsk(ShellCommand cmd) {
        if (currentSessionId == null) {
            ShellPrinter.printError("No active session. Run 'analyze <path>' first.");
            return true;
        }
        Optional<SessionContext> ctxOpt = orchestrationService.getSession(currentSessionId);
        if (ctxOpt.isEmpty()) {
            ShellPrinter.printError("Session not found: " + currentSessionId);
            return true;
        }
        SessionContext ctx = ctxOpt.get();

        ShellPrinter.print("  [Answering...]");
        String answer = orchestrationService.ask(ctx, cmd.argument());
        ShellPrinter.printSeparator();
        ShellPrinter.print(answer);
        ShellPrinter.printSeparator();
        return true;
    }

    private boolean executeHistory() {
        if (currentSessionId == null) {
            ShellPrinter.print("No active session.");
            return true;
        }
        List<String> history = orchestrationService.getHistory(currentSessionId);
        ShellPrinter.printHistory(history);
        return true;
    }

    private boolean executeSessions() {
        var sessions = orchestrationService.listSessions();
        ShellPrinter.printSessions(sessions);
        return true;
    }

    private boolean executeSwitch(ShellCommand cmd) {
        String id = cmd.argument();
        if (orchestrationService.getSession(id).isPresent()) {
            currentSessionId = id;
            ShellPrinter.print("Switched to session: " + id);
        } else {
            ShellPrinter.printError("Session not found: " + id);
        }
        return true;
    }

    private boolean executeRetry(ShellCommand cmd) {
        ShellPrinter.printError("Retry command not yet implemented in v2.0");
        return true;
    }

    private boolean executeHelp() {
        ShellPrinter.printHelp();
        return true;
    }

    private boolean executeExit() {
        if (currentSessionId != null) {
            orchestrationService.removeSession(currentSessionId);
        }
        ShellPrinter.print("Goodbye!");
        return false;
    }

    private void printCachedResults(SessionContext ctx) {
        ctx.getCachedResearch().ifPresent(r -> {
            ShellPrinter.print("  [Research]");
            ShellPrinter.print("  Project Files:");
            ShellPrinter.print("  " + truncate(r.directoryTree(), 1500));
            ShellPrinter.print("  Matched Snippets (" + r.matchedSnippets().size() + "):");
            r.matchedSnippets().forEach(s ->
                    ShellPrinter.print("    - " + s.filePath() + ":" + s.lineNumber()));
            ShellPrinter.print("  Key Dependencies (" + r.dependencyInfo().keyDependencies().size() + "): "
                    + String.join(", ", r.dependencyInfo().keyDependencies()));
            if (r.gitStatus() != null && !r.gitStatus().isBlank()) {
                ShellPrinter.print("  Git Status:\n" + r.gitStatus());
            }
        });
        ctx.getCachedPlan().ifPresent(p -> {
            ShellPrinter.print("  [Plan] risk=" + p.riskLevel());
            p.suggestions().forEach(s -> ShellPrinter.print("  - " + s));
        });
        ctx.getCachedTestPlan().ifPresent(t -> {
            ShellPrinter.print("  [Test Plan] (" + t.testCases().size() + " cases)");
            t.testCases().forEach(tc -> ShellPrinter.print("  - " + tc));
        });
    }

    /** 截断过长文本，避免刷屏 */
    private static String truncate(String s, int maxLen) {
        if (s == null) return "(null)";
        return s.length() > maxLen ? s.substring(0, maxLen) + "\n  ... (truncated)" : s;
    }
}
