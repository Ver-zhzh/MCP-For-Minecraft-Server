# Minecraft Server MCP

[English](#english) | [中文](#中文)

---

<a name="english"></a>
## 📖 English Documentation

### Overview

**Minecraft Server MCP** is a Model Context Protocol (MCP) server implementation that enables AI assistants to interact with and manage Minecraft servers through a standardized interface. This project bridges AI capabilities with Minecraft server administration, making it easy to:

- 🎮 Monitor server status and performance in real-time
- 👥 Manage players and query online status
- 🔌 View and manage installed plugins
- 📝 Execute server commands programmatically
- 📊 Access and filter server logs, errors, and warnings
- 🔗 Connect to multiple Minecraft servers simultaneously

### Architecture

The project consists of two main components:

1. **Python MCP Server** (`src/smc/`) - Implements the Model Context Protocol, providing a standardized interface for AI assistants
2. **Java Minecraft Plugin** (`plugin/`) - Runs inside your Minecraft server, exposing an HTTP API for server management

### Table of Contents

- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Python MCP Server Setup](#python-mcp-server-setup)
  - [Minecraft Plugin Setup](#minecraft-plugin-setup)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Core Features](#core-features)
  - [Connection Management](#connection-management)
  - [Server Monitoring](#server-monitoring)
  - [Player Management](#player-management)
  - [Log Analysis](#log-analysis)
  - [Command Execution](#command-execution)
- [Available Tools](#available-tools)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

---

### Installation

#### Prerequisites

- **Python**: 3.12 or higher
- **Minecraft Server**: Spigot/Paper 1.8.9-1.21.x (excluding 1.17)
- **Java**: JDK 8 or higher (for building the plugin)
- **Maven**: For building the Minecraft plugin

#### Python MCP Server Setup

We recommend using [uv](https://docs.astral.sh/uv/) for Python dependency management.

1. **Clone the repository:**

```bash
git clone https://github.com/your-username/minecraft-server-mcp.git
cd minecraft-server-mcp
```

2. **Install dependencies:**

```bash
# Using uv (recommended)
uv sync

# Or using pip
pip install -e .
```

3. **Verify installation:**

```bash
# Using uv
uv run python -m smc

# Or using pip
python -m smc
```

#### Minecraft Plugin Setup

1. **Build the plugin:**

```bash
cd plugin
mvn clean package
```

2. **Install the plugin:**

Copy the generated JAR file to your Minecraft server's plugins folder:

```bash
cp target/SMC-Plugin.jar /path/to/your/minecraft/server/plugins/
```

3. **Configure the plugin:**

Edit `plugins/SMC-Plugin/config.yml` in your Minecraft server directory:

```yaml
# HTTP API Configuration
http:
  port: 8080
  host: "0.0.0.0"

# Security
security:
  api_key: "your-secure-api-key-here"
  
# Logging
logging:
  level: INFO
```

4. **Restart your Minecraft server** to load the plugin.

---

### Quick Start

#### 1. Start the Minecraft Server

Ensure your Minecraft server is running with the SMC Plugin installed.

#### 2. Configure MCP Server

Set environment variables or create a configuration file:

```bash
export MINECRAFT_LOG_LEVEL=INFO
export MINECRAFT_LOG_FILE=smc-server.log
```

#### 3. Run the MCP Server

```bash
# Using uv
uv run python -m smc

# Or using pip
python -m smc
```

#### 4. Connect to Your Minecraft Server

Use the MCP client to connect:

```json
{
  "tool": "connect",
  "arguments": {
    "url": "http://localhost:8080",
    "api_key": "your-secure-api-key-here",
    "timeout": 10
  }
}
```

#### 5. Query Server Status

```json
{
  "tool": "status",
  "arguments": {
    "api_key": "your-secure-api-key-here"
  }
}
```

---

### Configuration

#### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `MINECRAFT_LOG_LEVEL` | Logging level (DEBUG, INFO, WARNING, ERROR) | `ERROR` |
| `MINECRAFT_LOG_FILE` | Path to log file | `smc-server.log` |

#### Plugin Configuration

Located at `plugins/SMC-Plugin/config.yml`:

```yaml
http:
  port: 8080              # HTTP API port
  host: "0.0.0.0"         # Bind address

security:
  api_key: "your-key"     # API authentication key

logging:
  level: INFO             # Plugin log level
  max_entries: 1000       # Maximum log entries to keep
```

---

### Core Features

#### Connection Management

Connect to multiple Minecraft servers using unique API keys:

- **`connect`** - Establish connection to a server
- **`disconnect`** - Close connection to a server
- **`list_servers`** - View all active connections

#### Server Monitoring

Get real-time server information:

- **`status`** - Server version, online status, and basic info
- **`plugins`** - List installed plugins with versions and authors

#### Player Management

Track and manage players:

- **`player_list`** - Get online players with UUID and ping information

#### Log Analysis

Access comprehensive server logs:

- **`get_logs`** - Retrieve logs with time-based filtering
- **`get_errors`** - Filter error-level logs by plugin
- **`get_warnings`** - Filter warning-level logs

#### Command Execution

Execute server commands programmatically:

- **`send_command`** - Execute single or multiple commands
- **`get_commands`** - List all available server commands

---

### Available Tools

| Tool Name | Description | Required Parameters |
|-----------|-------------|---------------------|
| `connect` | Connect to a Minecraft server | `url`, `api_key` |
| `disconnect` | Disconnect from a server | `api_key` |
| `list_servers` | List all connected servers | None |
| `status` | Get server status and information | `api_key` |
| `plugins` | Get installed plugins list | `api_key` |
| `send_command` | Execute server commands | `api_key`, `commands` |
| `get_logs` | Retrieve server logs | `api_key` |
| `player_list` | Get online players | `api_key` |
| `get_errors` | Get error logs | `api_key` |
| `get_warnings` | Get warning logs | `api_key` |
| `get_commands` | List server commands | `api_key` |

---

### Development

#### Running Tests

```bash
# Python tests
uv run pytest

# Java plugin tests
cd plugin
mvn test
```

#### Project Structure

```
minecraft-server-mcp/
├── src/smc/                    # Python MCP server
│   ├── __init__.py
│   ├── server.py               # Main MCP server
│   ├── client.py               # HTTP client for plugin API
│   ├── tools.py                # MCP tool implementations
│   ├── config.py               # Configuration management
│   └── connection_manager.py   # Multi-server connection manager
├── plugin/                     # Java Minecraft plugin
│   ├── src/main/java/
│   │   └── com/smc/            # Plugin source code
│   ├── src/main/resources/
│   │   └── plugin.yml          # Plugin metadata
│   └── pom.xml                 # Maven configuration
├── .python-version             # Python version (3.12)
├── uv.lock                     # Dependency lock file
└── README.md                   # This file
```

---

### Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

### License

This project is licensed under the MIT License - see the LICENSE file for details.

---

<a name="中文"></a>
## 📖 中文文档

### 概述

**Minecraft Server MCP** 是一个 Model Context Protocol (MCP) 服务器实现，使 AI 助手能够通过标准化接口与 Minecraft 服务器进行交互和管理。该项目将 AI 能力与 Minecraft 服务器管理相结合，轻松实现：

- 🎮 实时监控服务器状态和性能
- 👥 管理玩家并查询在线状态
- 🔌 查看和管理已安装的插件
- 📝 程序化执行服务器命令
- 📊 访问和过滤服务器日志、错误和警告
- 🔗 同时连接多个 Minecraft 服务器

### 架构

项目由两个主要组件组成:

1. **Python MCP 服务器** (`src/smc/`) - 实现 Model Context Protocol，为 AI 助手提供标准化接口
2. **Java Minecraft 插件** (`plugin/`) - 在 Minecraft 服务器中运行，提供 HTTP API 用于服务器管理

### 目录

- [安装](#安装)
  - [前置要求](#前置要求)
  - [Python MCP 服务器设置](#python-mcp-服务器设置)
  - [Minecraft 插件设置](#minecraft-插件设置)
- [快速开始](#快速开始)
- [配置](#配置)
- [核心功能](#核心功能)
  - [连接管理](#连接管理)
  - [服务器监控](#服务器监控)
  - [玩家管理](#玩家管理)
  - [日志分析](#日志分析)
  - [命令执行](#命令执行)
- [可用工具](#可用工具)
- [开发](#开发)
- [贡献](#贡献)
- [许可证](#许可证)

---

### 安装

#### 前置要求

- **Python**: 3.12 或更高版本
- **Minecraft 服务器**: Spigot/Paper 1.8.9-1.21.x（不支持 1.17）
- **Java**: JDK 8 或更高版本（用于构建插件）
- **Maven**: 用于构建 Minecraft 插件

#### Python MCP 服务器设置

我们推荐使用 [uv](https://docs.astral.sh/uv/) 进行 Python 依赖管理。

1. **克隆仓库:**

```bash
git clone https://github.com/your-username/minecraft-server-mcp.git
cd minecraft-server-mcp
```

2. **安装依赖:**

```bash
# 使用 uv（推荐）
uv sync

# 或使用 pip
pip install -e .
```

3. **验证安装:**

```bash
# 使用 uv
uv run python -m smc

# 或使用 pip
python -m smc
```

#### Minecraft 插件设置

1. **构建插件:**

```bash
cd plugin
mvn clean package
```

2. **安装插件:**

将生成的 JAR 文件复制到 Minecraft 服务器的 plugins 文件夹:

```bash
cp target/SMC-Plugin.jar /path/to/your/minecraft/server/plugins/
```

3. **配置插件:**

在 Minecraft 服务器目录中编辑 `plugins/SMC-Plugin/config.yml`:

```yaml
# HTTP API 配置
http:
  port: 8080
  host: "0.0.0.0"

# 安全设置
security:
  api_key: "your-secure-api-key-here"
  
# 日志设置
logging:
  level: INFO
```

4. **重启 Minecraft 服务器** 以加载插件。

---

### 快速开始

#### 1. 启动 Minecraft 服务器

确保您的 Minecraft 服务器正在运行且已安装 SMC 插件。

#### 2. 配置 MCP 服务器

设置环境变量或创建配置文件:

```bash
export MINECRAFT_LOG_LEVEL=INFO
export MINECRAFT_LOG_FILE=smc-server.log
```

#### 3. 运行 MCP 服务器

```bash
# 使用 uv
uv run python -m smc

# 或使用 pip
python -m smc
```

#### 4. 连接到您的 Minecraft 服务器

使用 MCP 客户端连接:

```json
{
  "tool": "connect",
  "arguments": {
    "url": "http://localhost:8080",
    "api_key": "your-secure-api-key-here",
    "timeout": 10
  }
}
```

#### 5. 查询服务器状态

```json
{
  "tool": "status",
  "arguments": {
    "api_key": "your-secure-api-key-here"
  }
}
```

---

### 配置

#### 环境变量

| 变量 | 描述 | 默认值 |
|------|------|--------|
| `MINECRAFT_LOG_LEVEL` | 日志级别 (DEBUG, INFO, WARNING, ERROR) | `ERROR` |
| `MINECRAFT_LOG_FILE` | 日志文件路径 | `smc-server.log` |

#### 插件配置

位于 `plugins/SMC-Plugin/config.yml`:

```yaml
http:
  port: 8080              # HTTP API 端口
  host: "0.0.0.0"         # 绑定地址

security:
  api_key: "your-key"     # API 认证密钥

logging:
  level: INFO             # 插件日志级别
  max_entries: 1000       # 保留的最大日志条目数
```

---

### 核心功能

#### 连接管理

使用唯一的 API 密钥连接到多个 Minecraft 服务器:

- **`connect`** - 建立到服务器的连接
- **`disconnect`** - 关闭到服务器的连接
- **`list_servers`** - 查看所有活动连接

#### 服务器监控

获取实时服务器信息:

- **`status`** - 服务器版本、在线状态和基本信息
- **`plugins`** - 列出已安装的插件及版本和作者

#### 玩家管理

跟踪和管理玩家:

- **`player_list`** - 获取在线玩家及其 UUID 和延迟信息

#### 日志分析

访问全面的服务器日志:

- **`get_logs`** - 使用基于时间的过滤检索日志
- **`get_errors`** - 按插件过滤错误级别日志
- **`get_warnings`** - 过滤警告级别日志

#### 命令执行

程序化执行服务器命令:

- **`send_command`** - 执行单个或多个命令
- **`get_commands`** - 列出所有可用的服务器命令

---

### 可用工具

| 工具名称 | 描述 | 必需参数 |
|---------|------|----------|
| `connect` | 连接到 Minecraft 服务器 | `url`, `api_key` |
| `disconnect` | 从服务器断开连接 | `api_key` |
| `list_servers` | 列出所有已连接的服务器 | 无 |
| `status` | 获取服务器状态和信息 | `api_key` |
| `plugins` | 获取已安装插件列表 | `api_key` |
| `send_command` | 执行服务器命令 | `api_key`, `commands` |
| `get_logs` | 检索服务器日志 | `api_key` |
| `player_list` | 获取在线玩家 | `api_key` |
| `get_errors` | 获取错误日志 | `api_key` |
| `get_warnings` | 获取警告日志 | `api_key` |
| `get_commands` | 列出服务器命令 | `api_key` |

---

### 开发

#### 运行测试

```bash
# Python 测试
uv run pytest

# Java 插件测试
cd plugin
mvn test
```

#### 项目结构

```
minecraft-server-mcp/
├── src/smc/                    # Python MCP 服务器
│   ├── __init__.py
│   ├── server.py               # 主 MCP 服务器
│   ├── client.py               # 插件 API 的 HTTP 客户端
│   ├── tools.py                # MCP 工具实现
│   ├── config.py               # 配置管理
│   └── connection_manager.py   # 多服务器连接管理器
├── plugin/                     # Java Minecraft 插件
│   ├── src/main/java/
│   │   └── com/smc/            # 插件源代码
│   ├── src/main/resources/
│   │   └── plugin.yml          # 插件元数据
│   └── pom.xml                 # Maven 配置
├── .python-version             # Python 版本 (3.12)
├── uv.lock                     # 依赖锁定文件
└── README.md                   # 本文件
```

---

### 贡献

欢迎贡献！请遵循以下指南:

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开 Pull Request

---

### 许可证

本项目采用 MIT 许可证 - 详情请参见 LICENSE 文件。

---

## 🌟 Star History

If you find this project helpful, please consider giving it a star! ⭐

如果您觉得这个项目有帮助，请考虑给它一个星标！⭐
