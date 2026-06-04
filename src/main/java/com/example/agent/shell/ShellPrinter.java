package com.example.agent.shell;

import com.example.agent.dto.SessionSnapshot;

import java.util.List;

/**
 * Shell 格式化输出工具。
 */
public class ShellPrinter {

    private static final String SEPARATOR = "  " + "-".repeat(60);
    private static final String BANNER = """
            ╔══════════════════════════════════════════════════╗
            ║   Spring AI Code Agent Shell v2.0               ║
            ║   Type 'help' for available commands            ║
            ╚══════════════════════════════════════════════════╝
            """;

    public static void printBanner() {
        System.out.println(BANNER);
    }

    public static void printHelp() {
        System.out.println("""
                Available commands:
                  analyze <path> [--requirement "..."]  分析指定项目
                  <任意文本>                              多轮追问（基于当前分析结果）
                  retry <number>                         重新执行历史记录中的指令
                  history                                查看当前对话历史
                  sessions                               列出所有活跃会话
                  switch <sessionId>                     切换到指定会话
                  help                                   显示此帮助信息
                  exit                                   退出 Shell
                """);
    }

    public static void printSeparator() {
        System.out.println(SEPARATOR);
    }

    public static void print(String msg) {
        System.out.println(msg);
    }

    public static void printError(String msg) {
        System.out.println("[Error] " + msg);
    }

    public static void printSessions(List<SessionSnapshot> sessions) {
        if (sessions.isEmpty()) {
            System.out.println("No active sessions.");
            return;
        }
        System.out.println("Active sessions:");
        for (SessionSnapshot s : sessions) {
            System.out.printf("  %s  |  %s  |  %s  |  %d msgs |  %s%n",
                    s.sessionId(),
                    s.projectPath(),
                    s.createdAt().toLocalTime().toString().substring(0, 5),
                    s.messageCount(),
                    s.lastCommand() != null ? s.lastCommand() : "");
        }
    }

    public static void printHistory(List<String> history) {
        if (history == null || history.isEmpty()) {
            System.out.println("No history in this session.");
            return;
        }
        for (int i = 0; i < history.size(); i++) {
            System.out.println((i + 1) + ": " + history.get(i));
        }
    }

    public static void printTodoProgress(List<String> statuses) {
        for (String s : statuses) {
            System.out.println("  " + s);
        }
    }
}
