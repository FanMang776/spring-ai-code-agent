package com.example.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Code Agent 应用启动类。
 * <p>
 * 本应用是一个基于 Spring AI 的智能代码研发助手，通过 CLI 命令行接收用户需求，
 * 利用多 Agent 协作完成代码分析、方案规划和测试验证，最终输出实施建议报告。
 * </p>
 *
 * @author FanMang776
 * @see com.example.agent.cli.AnalyzeCommand
 */
@SpringBootApplication
public class SpringAiCodeAgentApplication {

    /**
     * 应用程序入口方法。
     *
     * @param args 命令行参数，支持格式：analyze "需求描述" [--path 项目路径]
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringAiCodeAgentApplication.class, args);
    }
}
