package com.example.agent.config;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * JLine3 终端配置。
 * <p>
 * 配置 REPL Shell 所需的 Terminal 和 LineReader Bean，
 * 支持历史记录、Tab 补全和其他终端交互功能。
 * </p>
 */
@Configuration
public class ShellConfig {

    @Bean
    public Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .system(true)
                .build();
    }

    @Bean
    public History shellHistory() {
        return new DefaultHistory();
    }

    @Bean
    public LineReader lineReader(Terminal terminal, History shellHistory) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .history(shellHistory)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .variable(LineReader.HISTORY_FILE, java.nio.file.Paths.get(System.getProperty("user.home"), ".code-agent-history"))
                .build();
    }
}
