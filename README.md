# Spring AI Code Agent

> 基于 Spring AI 的智能代码研发助手，使用 DeepSeek 大语言模型驱动的多 Agent 协作系统。

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring_AI-1.0.0-green.svg)](https://spring.io/projects/spring-ai)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## 📋 项目简介

本项目是一个 **AI Agent 编排系统**，通过多角色子代理协作完成代码分析与方案规划。系统支持两种运行模式：

### 🖥️ Shell 模式（v2.0 新增）
启动后进入交互式 REPL Shell，支持多轮对话、多会话管理：
```bash
java -jar target/spring-ai-code-agent.jar
```
```
╔══════════════════════════════════════════════════╗
║   Spring AI Code Agent Shell v2.0               ║
║   Type 'help' for available commands            ║
╚══════════════════════════════════════════════════╝

agent> analyze E:/my-project --requirement "增加Redis缓存层"
  [分析完成，输出结构化报告]

agent> 这个方案的第三点风险高吗？
  [基于分析结果的多轮追问]
```

### ⌨️ CLI 模式（向后兼容）
```bash
java -jar app.jar analyze "需求描述" --path /path/to/project
```

---

## 🏗️ 架构设计

### 模块结构

```
src/main/java/com/example/agent/
├── SpringAiCodeAgentApplication.java   # 启动入口（Shell / CLI 双模式）
├── advisor/                             # Advisor 切面链（v2.0 新增）
│   ├── LoggingAdvisor.java             # 日志追踪 + Token 统计
│   ├── RetryAdvisor.java               # 指数退避自动重试
│   ├── ContentFilterAdvisor.java       # 敏感内容过滤脱敏
│   └── ValidationAdvisor.java          # 结构化输出字段校验
├── cli/                                 # CLI 入口层
│   ├── CliInput.java                    # 命令行参数解析
│   └── AnalyzeCommand.java              # 启动入口 (CommandLineRunner)
├── config/                              # 全局配置（v2.0 新增）
│   ├── ChatClientConfig.java           # ChatClient Bean + Advisor 注册
│   └── ShellConfig.java                # JLine3 终端配置
├── core/                                # 核心编排层
│   ├── SessionContext.java             # 会话上下文（v2.0 新增）
│   ├── SessionOrchestrationService.java# 会话感知编排服务（v2.0 新增）
│   ├── OrchestrationService.java       # 兼容的编排服务
│   ├── AgentTodoTracker.java           # 任务进度跟踪器
│   └── SubAgentResult.java             # 子代理结果数据类
├── dto/                                 # 结构化 DTO（v2.0 新增）
│   ├── ResearchResult.java             # 代码研究结构化结果
│   ├── PlanResult.java                 # 方案规划结构化结果
│   ├── TestPlanResult.java             # 测试验证结构化结果
│   └── ...
└── shell/                               # REPL Shell 层（v2.0 新增）
│   ├── AgentShell.java                 # REPL 主循环
│   ├── ShellCommand.java               # 命令解析器
│   └── ShellPrinter.java               # 格式化输出
└── subagents/                           # 子代理层
    ├── CodeResearchSubAgent.java        # 代码研究代理 → ResearchResult
    ├── SolutionPlannerSubAgent.java     # 方案规划代理 → PlanResult (.entity())
    └── TestVerifierSubAgent.java        # 测试验证代理 → TestPlanResult (.entity())
```

### 子代理职责

| 子代理 | 职责 | 输出类型 | 技术亮点 |
|--------|------|----------|----------|
| **CodeResearchSubAgent** | 项目结构分析、代码内容搜索、Git 状态检查 | `ResearchResult` | 结构化 DTO 输出 |
| **SolutionPlannerSubAgent** | 基于研究结果生成变更方案与实施建议 | `PlanResult` | `.entity()` 自动映射 |
| **TestVerifierSubAgent** | 制定测试策略、定义验收标准 | `TestPlanResult` | `.entity()` 自动映射 |

### 技术链

| 技术 | 版本 | 用途 | Spring AI 特性 |
|------|------|------|----------------|
| **ChatClient** | 1.0.0 | LLM 统一调用入口 | `.entity()` 结构化输出 |
| **Advisor 链** | 1.0.0 | 日志 / 重试 / 安全 / 校验 | `CallAdvisor` / `BaseAdvisor` |
| **ChatMemory** | 1.0.0 | 多轮对话记忆 | `MessageWindowChatMemory` |
| **Streaming** | 1.0.0 | 流式输出（可扩展） | `Flux<String>` |

---

## ⚙️ 环境要求

- **JDK**: 17+
- **Maven**: 3.8+
- **DeepSeek API Key**（用于 LLM 调用）

---

## 🚀 快速开始

### 1. 配置 API Key

```bash
export DEEPSEEK_API_KEY=your-api-key
export DEEPSEEK_BASE_URL=https://api.deepseek.com
export DEEPSEEK_MODEL=deepseek-chat
```

### 2. 构建

```bash
mvn clean package -DskipTests
```

### 3. 运行

**Shell 模式（推荐）**：
```bash
java -jar target/spring-ai-code-agent.jar
```

**CLI 模式**：
```bash
java -jar target/spring-ai-code-agent.jar analyze "增加日志" --path /my/project
```

---

## 🔧 Shell 命令参考

| 命令 | 说明 |
|------|------|
| `analyze <path> --requirement "xxx"` | 执行完整分析（三步流水线） |
| `<任意文本>` | 基于当前分析结果多轮追问 |
| `retry <number>` | 重新执行历史中的某次命令 |
| `history` | 查看当前会话历史 |
| `sessions` | 列出所有活跃会话 |
| `switch <sessionId>` | 切换到指定会话 |
| `help` | 显示帮助信息 |
| `exit` | 退出 Shell |

---

## 📊 测试覆盖

| 模块 | 测试数 | 覆盖内容 |
|------|--------|----------|
| Advisor | 8 | 日志/重试/过滤/校验 四组件 |
| CliInput | 3 | 命令行参数解析 |
| ShellCommand | 12 | 所有命令格式解析 |
| SessionContext | 6 | 缓存/元数据/记忆 |

---

## 🔬 亮点

1. **多 Agent 编排** - Spring AI + DeepSeek 实现从研究→规划→测试的完整流水线
2. **Advisor 切面链** - 责任链模式实现横切关注点，展示 Spring AI 1.0+ 特性
3. **Structured Outputs** - LLM 输出直接映射 Java Record，展示格式即类型安全
4. **REPL 交互** - JLine3 实现交互式 Shell，展示对话记忆与会话管理
5. **TDD 开发** - 所有组件均采用 TDD 方式开发，测试覆盖率 29 个用例 0 失败

---

## 📄 License

Apache License 2.0
