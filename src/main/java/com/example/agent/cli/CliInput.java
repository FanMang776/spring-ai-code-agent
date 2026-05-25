package com.example.agent.cli;

public record CliInput(boolean analyze, String requirement, String path) {

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
        for (int i = 2; i < args.length; i++) {
            if ("--path".equals(args[i]) && i + 1 < args.length) {
                path = args[i + 1];
                i++;
            }
        }
        return new CliInput(true, requirement, path);
    }

    private static String sanitizeRequirement(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        if (text.length() >= 2 && text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        if (text.length() >= 2 && text.startsWith("\\") && text.endsWith("\\")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        return text;
    }
}
