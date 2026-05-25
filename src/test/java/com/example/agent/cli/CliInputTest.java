package com.example.agent.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliInputTest {

    @Test
    void shouldParseAnalyzeWithoutPath() {
        CliInput input = CliInput.parse(new String[]{"analyze", "实现一个缓存模块"});
        assertTrue(input.analyze());
        assertEquals("实现一个缓存模块", input.requirement());
        assertEquals(null, input.path());
    }

    @Test
    void shouldParseAnalyzeWithPath() {
        CliInput input = CliInput.parse(new String[]{"analyze", "需求A", "--path", "E:\\repo"});
        assertTrue(input.analyze());
        assertEquals("需求A", input.requirement());
        assertEquals("E:\\repo", input.path());
    }

    @Test
    void shouldSanitizeWrappedRequirement() {
        CliInput input = CliInput.parse(new String[]{"analyze", "\\检查项目结构和潜在问题\\"});
        assertTrue(input.analyze());
        assertEquals("检查项目结构和潜在问题", input.requirement());
    }
}
