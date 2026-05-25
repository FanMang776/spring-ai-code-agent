package com.example.agent.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CliInput} 命令行解析器的单元测试。
 * <p>
 * 覆盖以下场景：
 * <ul>
 *   <li>不带 --path 参数的基本解析</li>
 *   <li>带 --path 参数的完整解析</li>
 *   <li>带反斜杠包裹的需求文本清理</li>
 * </ul>
 * </p>
 *
 * @author FanMang776
 */
class CliInputTest {

    /**
     * 测试基本解析：仅包含 analyze 命令和需求描述（无 --path）。
     * <p>
     * 验证点：
     * <ul>
     *   <li>analyze 标志为 true</li>
     *   <li>需求文本正确提取</li>
     *   <li>path 为 null（使用默认当前目录）</li>
     * </ul>
     * </p>
     */
    @Test
    void shouldParseAnalyzeWithoutPath() {
        CliInput input = CliInput.parse(new String[]{"analyze", "实现一个缓存模块"});
        assertTrue(input.analyze());
        assertEquals("实现一个缓存模块", input.requirement());
        assertEquals(null, input.path());
    }

    /**
     * 测试完整解析：包含 analyze 命令、需求描述和 --path 参数。
     * <p>
     * 验证点：
     * <ul>
     *   <li>analyze 标志为 true</li>
     *   <li>需求文本正确提取</li>
     *   <li>path 正确解析为指定路径</li>
     * </ul>
     * </p>
     */
    @Test
    void shouldParseAnalyzeWithPath() {
        CliInput input = CliInput.parse(new String[]{"analyze", "需求A", "--path", "E:\\repo"});
        assertTrue(input.analyze());
        assertEquals("需求A", input.requirement());
        assertEquals("E:\\repo", input.path());
    }

    /**
     * 测试需求文本的反斜杠包裹清理。
     * <p>
     * Windows CMD 环境下，用户可能用反斜杠包裹参数（如 \检查项目结构和潜在问题\），
     * 应能正确剥离包裹符号得到纯文本。
     * </p>
     */
    @Test
    void shouldSanitizeWrappedRequirement() {
        CliInput input = CliInput.parse(new String[]{"analyze", "\\检查项目结构和潜在问题\\"});
        assertTrue(input.analyze());
        assertEquals("检查项目结构和潜在问题", input.requirement());
    }
}
