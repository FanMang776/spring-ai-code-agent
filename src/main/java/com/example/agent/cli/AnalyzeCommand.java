package com.example.agent.cli;

import com.example.agent.core.OrchestrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Analyze 命令执行入口（CLI 层）。
 * <p>
 * 实现 Spring Boot 的 {@link CommandLineRunner} 接口，
 * 在应用启动完成后自动执行命令解析和分析流程。
 * </p>
 * <p>
 * 仅当第一个命令行参数为 "analyze" 时触发分析逻辑，
 * 否则静默退出（允许其他潜在命令扩展）。
 * </p>
 *
 * @author FanMang776
 * @see OrchestrationService
 * @see CliInput
 */
@Component
public class AnalyzeCommand implements CommandLineRunner {

    /** 核心编排服务 */
    private final OrchestrationService orchestrationService;

    /**
     * 构造函数，注入编排服务。
     *
     * @param orchestrationService 多 Agent 编排服务实例
     */
    public AnalyzeCommand(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    /**
     * Spring Boot 启动后执行的回调方法。
     * <ol>
     *   <li>解析命令行输入</li>
     *   <li>判断是否为 analyze 命令</li>
     *   <li>确定目标路径（显式指定或使用当前目录）</li>
     *   <li>调用编排服务执行分析</li>
     *   <li>将 Markdown 报告输出到控制台</li>
     * </ol>
     *
     * @param args 命令行参数数组
     * @throws Exception 分析过程中发生的异常
     */
    @Override
    public void run(String... args) throws Exception {
        // 解析命令行参数
        CliInput input = CliInput.parse(args);

        // 非 analyze 命令则跳过
        if (!input.analyze()) {
            return;
        }

        // 确定目标项目路径（未指定则使用当前工作目录的绝对路径）
        Path target = input.path() != null ? Path.of(input.path()) : Path.of("").toAbsolutePath();

        // 执行分析并将报告输出到控制台
        String markdown = orchestrationService.analyze(input.requirement(), target);
        System.out.println(markdown);
    }
}
