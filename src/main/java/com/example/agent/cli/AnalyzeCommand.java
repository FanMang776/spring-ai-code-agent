package com.example.agent.cli;

import com.example.agent.core.OrchestrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class AnalyzeCommand implements CommandLineRunner {

    private final OrchestrationService orchestrationService;

    public AnalyzeCommand(OrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @Override
    public void run(String... args) throws Exception {
        CliInput input = CliInput.parse(args);
        if (!input.analyze()) {
            return;
        }
        Path target = input.path() != null ? Path.of(input.path()) : Path.of("").toAbsolutePath();
        String markdown = orchestrationService.analyze(input.requirement(), target);
        System.out.println(markdown);
    }
}
