package com.example.agent.core;

import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 任务进度跟踪器。
 * <p>
 * 基于 {@link TodoWriteTool} 实现，用于在多 Agent 执行过程中记录和展示任务进度。
 * 支持三种状态：待处理（pending）、进行中（in_progress）、已完成（completed）。
 * </p>
 * <p>
 * 每次状态变更都会自动调用 {@code flush()} 将最新进度同步到 TodoWriteTool。
 * </p>
 *
 * @author FanMang776
 * @see TodoWriteTool
 */
@Component
public class AgentTodoTracker {

    /** 底层 Todo 写入工具实例 */
    private final TodoWriteTool todoWriteTool;

    /** 当前 Todo 任务项列表 */
    private final List<TodoWriteTool.Todos.TodoItem> todoItems = new ArrayList<>();

    /**
     * 构造函数，创建默认的 TodoWriteTool 实例。
     */
    public AgentTodoTracker() {
        this.todoWriteTool = TodoWriteTool.builder().build();
    }

    /**
     * 初始化任务列表。
     * <p>
     * 清空现有任务项，并根据传入的内容列表创建新的待处理任务。
     * 初始化后自动刷新状态。
     * </p>
     *
     * @param contents 任务内容描述列表
     */
    public void init(List<String> contents) {
        todoItems.clear();
        for (String content : contents) {
            todoItems.add(new TodoWriteTool.Todos.TodoItem(content, TodoWriteTool.Todos.Status.pending, "准备执行: " + content));
        }
        flush();
    }

    /**
     * 将指定索引的任务标记为"进行中"状态。
     *
     * @param index      任务索引（从 0 开始）
     * @param activeForm 当前正在进行的操作描述
     */
    public void markInProgress(int index, String activeForm) {
        TodoWriteTool.Todos.TodoItem old = todoItems.get(index);
        todoItems.set(index, new TodoWriteTool.Todos.TodoItem(old.content(), TodoWriteTool.Todos.Status.in_progress, activeForm));
        flush();
    }

    /**
     * 将指定索引的任务标记为"已完成"状态。
     *
     * @param index      任务索引（从 0 开始）
     * @param activeForm 已完成的操作描述
     */
    public void markCompleted(int index, String activeForm) {
        TodoWriteTool.Todos.TodoItem old = todoItems.get(index);
        todoItems.set(index, new TodoWriteTool.Todos.TodoItem(old.content(), TodoWriteTool.Todos.Status.completed, activeForm));
        flush();
    }

    /**
     * 获取当前所有任务项的快照（不可变列表）。
     *
     * @return 任务项列表的不可变副本
     */
    public List<TodoWriteTool.Todos.TodoItem> snapshot() {
        return List.copyOf(todoItems);
    }

    /**
     * 将当前任务状态同步到底层 TodoWriteTool。
     * <p>
     * 每次 {@code init()}、{@code markInProgress()}、{@code markCompleted()}
     * 操作后都会自动调用此方法。
     * </p>
     */
    private void flush() {
        todoWriteTool.todoWrite(new TodoWriteTool.Todos(List.copyOf(todoItems)));
    }
}
