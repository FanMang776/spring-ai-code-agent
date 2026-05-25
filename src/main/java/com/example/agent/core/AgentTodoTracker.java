package com.example.agent.core;

import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentTodoTracker {

    private final TodoWriteTool todoWriteTool;
    private final List<TodoWriteTool.Todos.TodoItem> todoItems = new ArrayList<>();

    public AgentTodoTracker() {
        this.todoWriteTool = TodoWriteTool.builder().build();
    }

    public void init(List<String> contents) {
        todoItems.clear();
        for (String content : contents) {
            todoItems.add(new TodoWriteTool.Todos.TodoItem(content, TodoWriteTool.Todos.Status.pending, "准备执行: " + content));
        }
        flush();
    }

    public void markInProgress(int index, String activeForm) {
        TodoWriteTool.Todos.TodoItem old = todoItems.get(index);
        todoItems.set(index, new TodoWriteTool.Todos.TodoItem(old.content(), TodoWriteTool.Todos.Status.in_progress, activeForm));
        flush();
    }

    public void markCompleted(int index, String activeForm) {
        TodoWriteTool.Todos.TodoItem old = todoItems.get(index);
        todoItems.set(index, new TodoWriteTool.Todos.TodoItem(old.content(), TodoWriteTool.Todos.Status.completed, activeForm));
        flush();
    }

    public List<TodoWriteTool.Todos.TodoItem> snapshot() {
        return List.copyOf(todoItems);
    }

    private void flush() {
        todoWriteTool.todoWrite(new TodoWriteTool.Todos(List.copyOf(todoItems)));
    }
}
