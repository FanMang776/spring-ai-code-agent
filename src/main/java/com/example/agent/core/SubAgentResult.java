package com.example.agent.core;

/**
 * 子代理执行结果数据类。
 * <p>
 * 用于封装每个子代理的执行返回值，包含子代理名称、摘要信息和详细内容。
 * 作为各阶段之间的数据传递载体。
 * </p>
 *
 * @param name    子代理名称（如 "CodeResearch"、"SolutionPlanner"、"TestVerifier"）
 * @param summary 结果摘要（简短的一句话描述）
 * @param details 详细内容（Markdown 格式的完整分析/建议文本）
 * @author FanMang776
 */
public record SubAgentResult(String name, String summary, String details) {
}
