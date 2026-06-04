package com.example.agent.shell;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Shell 命令解析结果。
 *
 * @param type      命令类型
 * @param argument  命令参数
 * @param path      可选的目标路径参数（analyze 指定项目路径时）
 */
public record ShellCommand(Type type, String argument, Path path) {

    public enum Type {
        ANALYZE,    // analyze <path> [--requirement "xxx"]
        ASK,        // 其它任意输入视为追问
        HISTORY,    // history
        SESSIONS,   // sessions
        SWITCH,     // switch <sessionId>
        RETRY,      // retry <number>
        HELP,       // help
        EXIT;       // exit
    }

    /**
     * 解析用户输入的命令行文本。
     */
    public static ShellCommand parse(String line) {
        if (line == null || line.isBlank()) {
            return new ShellCommand(Type.ASK, "", null);
        }
        String trimmed = line.trim();

        // exit
        if (trimmed.equalsIgnoreCase("exit") || trimmed.equalsIgnoreCase("quit")) {
            return new ShellCommand(Type.EXIT, "", null);
        }
        // help
        if (trimmed.equalsIgnoreCase("help")) {
            return new ShellCommand(Type.HELP, "", null);
        }
        // history
        if (trimmed.equalsIgnoreCase("history")) {
            return new ShellCommand(Type.HISTORY, "", null);
        }
        // sessions
        if (trimmed.equalsIgnoreCase("sessions")) {
            return new ShellCommand(Type.SESSIONS, "", null);
        }
        // switch <sessionId>
        if (trimmed.toLowerCase().startsWith("switch ")) {
            String id = trimmed.substring("switch ".length()).trim();
            return new ShellCommand(Type.SWITCH, id, null);
        }
        // retry <number>
        if (trimmed.toLowerCase().startsWith("retry ")) {
            String num = trimmed.substring("retry ".length()).trim();
            return new ShellCommand(Type.RETRY, num, null);
        }
        // analyze <path> [--requirement "..."]
        if (trimmed.toLowerCase().startsWith("analyze ")) {
            return parseAnalyze(trimmed);
        }
        // 其它任意输入视为追问
        return new ShellCommand(Type.ASK, trimmed, null);
    }

    /** 去除首尾引号包裹 */
    private static String stripQuotes(String s) {
        if (s == null || s.length() < 2) return s;
        String t = s.trim();
        if (t.startsWith("\"") && t.endsWith("\"")) return t.substring(1, t.length() - 1).trim();
        if (t.startsWith("'") && t.endsWith("'")) return t.substring(1, t.length() - 1).trim();
        return t;
    }

    private static ShellCommand parseAnalyze(String trimmed) {
        String rest = trimmed.substring("analyze ".length()).trim();
        // 检查是否有 --requirement
        int reqIdx = rest.toLowerCase().indexOf("--requirement ");
        if (reqIdx >= 0) {
            String pathStr = restoreBackslashes(rest.substring(0, reqIdx).trim());
            String reqStr = stripQuotes(rest.substring(reqIdx + "--requirement ".length()).trim());
            Path path = Paths.get(pathStr.isEmpty() ? "." : pathStr);
            return new ShellCommand(Type.ANALYZE, reqStr, path);
        }
        // 没有 --requirement，参数就是 path
        String pathStr = restoreBackslashes(rest);
        Path path = Paths.get(pathStr);
        return new ShellCommand(Type.ANALYZE, "分析项目", path);
    }

    /**
     * 盘符容错补回反斜杠。
     * <p>
     * JLine3 的 {@code DISABLE_EVENT_EXPANSION} 已作为主要防御保留原始输入，
     * 此方法仅做盘符兜底：确保 {@code E:} 后面有路径分隔符。
     * </p>
     */
    private static String restoreBackslashes(String s) {
        if (s == null || s.isEmpty()) return s;
        // 仅盘符兜底：E: → E:\
        return s.replaceAll("^([A-Za-z]:)(?=[A-Za-z/])", "$1\\\\");
    }
}
