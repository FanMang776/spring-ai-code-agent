package com.example.agent.dto;

import java.time.LocalDateTime;

/**
 * 会话摘要信息。
 *
 * @param sessionId   会话唯一标识
 * @param projectPath 当前分析的项目路径
 * @param createdAt   会话创建时间
 * @param messageCount 对话历史消息数
 * @param lastCommand  最后执行的命令
 */
public record SessionSnapshot(
        String sessionId,
        String projectPath,
        LocalDateTime createdAt,
        int messageCount,
        String lastCommand) {
}
