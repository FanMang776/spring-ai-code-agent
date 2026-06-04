package com.example.agent;

import com.example.agent.cli.CliInput;
import com.example.agent.core.OrchestrationService;
import com.example.agent.shell.AgentShell;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;

/**
 * Spring AI Code Agent 应用启动类。
 * <p>
 * 两种运行模式：
 * <ul>
 *   <li><b>Shell 模式</b>（默认）：启动 REPL 交互式 Shell，用户可输入多条命令</li>
 *   <li><b>CLI 模式</b>（传入 analyze 参数）：一次性分析后退出，保持向后兼容</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class SpringAiCodeAgentApplication implements CommandLineRunner {

    private final AgentShell agentShell;
    private final OrchestrationService orchestrationService;

    public SpringAiCodeAgentApplication(AgentShell agentShell, OrchestrationService orchestrationService) {
        this.agentShell = agentShell;
        this.orchestrationService = orchestrationService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringAiCodeAgentApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        CliInput input = CliInput.parse(args);

        if (input.analyze()) {
            // CLI 模式：一次性分析后退出
            Path target = input.path() != null
                    ? Path.of(input.path())
                    : Path.of("").toAbsolutePath();
            String markdown = orchestrationService.analyze(input.requirement(), target);
            System.out.println(markdown);
        } else {
            // Shell 模式：启动 REPL
            agentShell.start();
        }
    }
}
