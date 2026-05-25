package com.example.agent.cli;

/**
 * 命令行输入解析器（Record 类型）。
 * <p>
 * 负责将命令行参数解析为结构化的输入对象。
 * 支持的命令格式：
 * <pre>
 *   analyze "需求描述"
 *   analyze "需求描述" --path &lt;项目路径&gt;
 * </pre>
 * </p>
 *
 * @param analyze    是否触发了 analyze 命令
 * @param requirement 解析后的需求描述文本
 * @param path       可选的目标项目路径（未指定时使用当前目录）
 * @author FanMang776
 */
public record CliInput(boolean analyze, String requirement, String path) {

    /**
     * 解析命令行参数为 CliInput 对象。
     * <p>
     * 解析规则：
     * <ul>
     *   <li>第一个参数必须是 "analyze"</li>
     *   <li>第二个参数是需求描述（支持引号包裹，会自动去除）</li>
     *   <li>--path 后跟目标项目路径（可选）</li>
     * </ul>
     * </p>
     *
     * @param args 原始命令行参数数组
     * @return 解析后的 CliInput 对象；若不符合格式要求则 analyze 为 false
     * @throws IllegalArgumentException 当缺少必需的需求参数时抛出
     */
    public static CliInput parse(String[] args) {
        if (args == null || args.length == 0) {
            return new CliInput(false, null, null);
        }
        if (!"analyze".equals(args[0])) {
            return new CliInput(false, null, null);
        }
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: analyze \"<requirement>\" [--path <projectPath>]");
        }
        String requirement = sanitizeRequirement(args[1]);
        String path = null;
        // 解析可选的 --path 参数
        for (int i = 2; i < args.length; i++) {
            if ("--path".equals(args[i]) && i + 1 < args.length) {
                path = args[i + 1];
                i++;
            }
        }
        return new CliInput(true, requirement, path);
    }

    /**
     * 清理需求描述文本。
     * <p>
     * 自动去除首尾空白，并移除可能存在的引号包裹（双引号、单引号、反斜杠）。
     * </p>
     *
     * @param raw 原始需求文本
     * @return 清理后的纯文本
     */
    private static String sanitizeRequirement(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        // 移除双引号包裹
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        // 移除单引号包裹
        if (text.length() >= 2 && text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        // 移除反斜杠包裹（Windows CMD 环境常见）
        if (text.length() >= 2 && text.startsWith("\\") && text.endsWith("\\")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        return text;
    }
}
