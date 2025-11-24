# Minecraft Server MCP

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Python 3.12+](https://img.shields.io/badge/python-3.12+-blue.svg)](https://www.python.org/downloads/)
[![Minecraft 1.8.9+](https://img.shields.io/badge/minecraft-1.8.9+-green.svg)](https://www.spigotmc.org/)

[English](#english) | [中文](#中文)

---

## English

A Model Context Protocol (MCP) server that enables AI assistants to manage Minecraft servers through natural language.

### Features

- 🎮 **Server Management** - Monitor status, manage plugins, execute commands
- 📊 **Log Analysis** - Query server logs, errors, and warnings
- 👥 **Player Management** - View online players and their information
- 🔒 **Secure** - API key authentication and configurable access control
- 🌐 **Multi-Version** - Supports Minecraft 1.8.9 - 1.21.x (excluding 1.17.x)

### Quick Start

1. **Install the Minecraft Plugin**
   ```bash
   cd plugin
   mvn clean package
   # Copy plugin/target/SMC-Plugin.jar to your server's plugins/ folder
   # Restart your server and copy the auto-generated API key from console
   ```

2. **Install the MCP Server**
   ```bash
   pip install -e .
   ```

3. **Configure**
   ```bash
   export MINECRAFT_PLUGIN_URL=http://localhost:8080
   export MINECRAFT_API_KEY=your-api-key-here
   ```

4. **Add to MCP Client**
   ```json
   {
     "mcpServers": {
       "minecraft": {
         "command": "python",
         "args": ["-m", "smc"],
         "env": {
           "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
           "MINECRAFT_API_KEY": "your-api-key-here"
         }
       }
     }
   }
   ```

### Usage Examples

```
User: "Is my Minecraft server running?"
AI: "Yes, your server is online running Paper 1.20.4"

User: "Give everyone a diamond sword"
AI: [Executes command] "Gave diamond swords to 5 players"

User: "Show me recent errors"
AI: "Found 2 errors: WorldEdit NullPointerException..."
```

### Available Tools

- `status` - Check server status and version
- `plugins` - List installed plugins
- `send_command` - Execute server commands
- `get_logs` - Retrieve server logs
- `player_list` - View online players
- `get_errors` - Query error logs
- `get_warnings` - Query warning logs
- `get_commands` - List available commands

### Requirements

**Python MCP Server:**
- Python 3.12+
- Dependencies: `mcp[cli]`, `httpx`, `pydantic`

**Minecraft Plugin:**
- Minecraft: Spigot/Paper 1.8.9+ (excluding 1.17.x)
- Java: 8+ (1.8-1.16) or 17+ (1.18-1.21)

### Documentation

- [Installation Guide](INSTALL.md) - Detailed installation steps
- [MCP Configuration](MCP_CONFIGURATION.md) - MCP server setup
- [Plugin Configuration](plugin/CONFIGURATION.md) - Plugin setup and security
- [Usage Examples](EXAMPLES.md) - Practical examples

### Security

- Use strong API keys (32+ characters)
- Default: localhost only (127.0.0.1)
- Configure command blacklist
- Review logs regularly

### License

MIT

---

## 中文

一个模型上下文协议（MCP）服务器，使AI助手能够通过自然语言管理Minecraft服务器。

### 功能特性

- 🎮 **服务器管理** - 监控状态、管理插件、执行命令
- 📊 **日志分析** - 查询服务器日志、错误和警告
- 👥 **玩家管理** - 查看在线玩家及其信息
- 🔒 **安全** - API密钥认证和可配置的访问控制
- 🌐 **多版本** - 支持Minecraft 1.8.9 - 1.21.x（不包括1.17.x）

### 快速开始

1. **安装Minecraft插件**
   ```bash
   cd plugin
   mvn clean package
   # 将 plugin/target/SMC-Plugin.jar 复制到服务器的 plugins/ 文件夹
   # 重启服务器并从控制台复制自动生成的API密钥
   ```

2. **安装MCP服务器**
   ```bash
   pip install -e .
   ```

3. **配置**
   ```bash
   export MINECRAFT_PLUGIN_URL=http://localhost:8080
   export MINECRAFT_API_KEY=你的API密钥
   ```

4. **添加到MCP客户端**
   ```json
   {
     "mcpServers": {
       "minecraft": {
         "command": "python",
         "args": ["-m", "smc"],
         "env": {
           "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
           "MINECRAFT_API_KEY": "你的API密钥"
         }
       }
     }
   }
   ```

### 使用示例

```
用户: "我的Minecraft服务器在运行吗？"
AI: "是的，您的服务器正在运行Paper 1.20.4"

用户: "给所有人一把钻石剑"
AI: [执行命令] "已给5名玩家钻石剑"

用户: "显示最近的错误"
AI: "发现2个错误：WorldEdit空指针异常..."
```

### 可用工具

- `status` - 检查服务器状态和版本
- `plugins` - 列出已安装的插件
- `send_command` - 执行服务器命令
- `get_logs` - 获取服务器日志
- `player_list` - 查看在线玩家
- `get_errors` - 查询错误日志
- `get_warnings` - 查询警告日志
- `get_commands` - 列出可用命令

### 系统要求

**Python MCP服务器：**
- Python 3.12+
- 依赖：`mcp[cli]`、`httpx`、`pydantic`

**Minecraft插件：**
- Minecraft：Spigot/Paper 1.8.9+（不包括1.17.x）
- Java：8+（1.8-1.16）或17+（1.18-1.21）

### 文档

- [安装指南](INSTALL.md) - 详细安装步骤
- [MCP配置](MCP_CONFIGURATION.md) - MCP服务器设置
- [插件配置](plugin/CONFIGURATION.md) - 插件设置和安全
- [使用示例](EXAMPLES.md) - 实用示例

### 安全性

- 使用强API密钥（32+字符）
- 默认：仅本地访问（127.0.0.1）
- 配置命令黑名单
- 定期检查日志

### 许可证

MIT
