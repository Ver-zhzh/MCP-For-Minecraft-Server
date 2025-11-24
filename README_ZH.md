# Minecraft Server MCP

通过AI助手管理Minecraft服务器的模型上下文协议（MCP）服务器。

## 概述

该项目使AI助手能够通过标准化的MCP接口与Minecraft服务器（Spigot/Paper）交互。它由两个协同工作的组件组成：

1. **MCP服务器**（Python）- 为AI助手提供MCP工具
2. **Minecraft插件**（Java）- 在Minecraft服务器上运行并提供HTTP API端点

该系统允许AI助手监控服务器状态、管理插件、执行命令、查询日志和管理玩家 - 所有这些都通过自然语言交互完成。

## 功能特性

### 核心功能
- **服务器状态监控** - 检查连接状态、服务器版本和在线状态
- **插件管理** - 列出所有已安装的插件及其版本和启用状态
- **命令执行** - 执行单个或多个服务器命令并捕获输出
- **日志查询** - 检索带有时间范围和限制过滤的服务器日志
- **玩家管理** - 获取在线玩家列表及详细信息
- **错误日志过滤** - 查询带有可选插件过滤的错误日志
- **警告日志过滤** - 查询带有可选插件过滤的警告日志

### 技术特性
- **版本兼容性** - 支持Minecraft 1.8.9至1.21.x（不包括1.17.x）
- **安全认证** - 所有请求基于API密钥的认证
- **异步操作** - 非阻塞HTTP通信
- **内存高效** - 日志存储的循环缓冲区（可配置大小）
- **线程安全** - 具有适当同步的并发请求处理

## 安装

### 快速开始

使用提供的脚本快速设置：

**Windows:**
```bash
quick-start.bat
```

**Linux/Mac:**
```bash
./quick-start.sh
```

这些脚本将构建两个组件并提供部署说明。

### 手动安装

#### 步骤1：安装Minecraft插件

1. **构建插件：**
   ```bash
   cd plugin
   mvn clean package
   ```
   或使用构建脚本：
   - Windows: `plugin\build.bat`
   - Linux/Mac: `./plugin/build.sh`

2. **在服务器上安装：**
   - 将`plugin/target/SMC-Plugin.jar`复制到Minecraft服务器的`plugins/`目录
   - 启动或重启Minecraft服务器
   - 插件将在`plugins/SMC/config.yml`创建默认配置

3. **获取自动生成的API密钥：**
   - 插件将在首次运行时自动生成安全的API密钥
   - 检查服务器控制台查看生成的密钥
   - 查找类似消息：`API Key: GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc`
   - 复制此密钥用于MCP服务器配置

4. **可选：自定义设置：**
   - 如果要更改其他设置，编辑`plugins/SMC/config.yml`
   - 使用`/reload confirm`重新加载或重启服务器

#### 步骤2：安装Python MCP服务器

1. **安装Python依赖：**
   ```bash
   # 使用uv（推荐）
   uv pip install -e .
   
   # 或使用pip
   pip install -e .
   ```

2. **配置环境变量：**
   ```bash
   # Linux/Mac
   export MINECRAFT_PLUGIN_URL=http://localhost:8080
   export MINECRAFT_API_KEY=你的安全API密钥
   
   # Windows (PowerShell)
   $env:MINECRAFT_PLUGIN_URL="http://localhost:8080"
   $env:MINECRAFT_API_KEY="你的安全API密钥"
   
   # Windows (CMD)
   set MINECRAFT_PLUGIN_URL=http://localhost:8080
   set MINECRAFT_API_KEY=你的安全API密钥
   ```

3. **添加到MCP客户端配置**（参见下面的配置部分）

## 配置

### MCP服务器

通过环境变量配置：

**必需：**
- `MINECRAFT_PLUGIN_URL` - Minecraft插件HTTP API的URL（例如：`http://localhost:8080`）
- `MINECRAFT_API_KEY` - 认证的API密钥（必须与插件配置匹配）

**可选：**
- `MINECRAFT_REQUEST_TIMEOUT` - HTTP请求超时（秒）（默认：10）
- `MINECRAFT_LOG_LEVEL` - 日志级别：DEBUG、INFO、WARNING、ERROR（默认：INFO）

### MCP配置示例

添加到MCP设置文件（例如：`mcp.json`）：

```json
{
  "mcpServers": {
    "minecraft": {
      "command": "python",
      "args": ["-m", "smc"],
      "env": {
        "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
        "MINECRAFT_API_KEY": "你的安全API密钥"
      }
    }
  }
}
```

### Minecraft插件

编辑Minecraft服务器上的`plugins/SMC/config.yml`：

```yaml
http:
  enabled: true
  host: "127.0.0.1"
  port: 8080
  api-key: "你的安全API密钥"

logging:
  buffer-size: 10000
  retention-hours: 24

commands:
  timeout-seconds: 30
  blacklist:
    - "stop"
    - "restart"
```

## 使用

配置完成后，MCP服务器为AI助手提供以下工具：

### 可用工具

#### 1. status
检查服务器连接并获取版本信息。

**示例：**
```
AI："我的Minecraft服务器状态如何？"
响应："服务器在线，运行Paper 1.20.4（Minecraft 1.20.4）"
```

#### 2. plugins
列出所有已安装的插件及其版本和状态。

**示例：**
```
AI："显示所有已安装的插件"
响应："您安装了5个插件：WorldEdit（7.2.15，已启用）、
      EssentialsX（2.20.1，已启用）..."
```

#### 3. send_command
执行一个或多个服务器命令。

**示例：**
```
AI："给所有玩家钻石剑"
执行命令："give @a diamond_sword 1"
响应："命令执行成功。给了3名玩家钻石剑。"
```

#### 4. get_logs
检索带有可选过滤的服务器日志。

**示例：**
```
AI："显示最后50条日志条目"
响应："这是最后50条日志条目：[时间戳] [INFO] 服务器已启动..."
```

#### 5. player_list
获取在线玩家列表。

**示例：**
```
AI："现在谁在线？"
响应："3名玩家在线：Steve（延迟：45ms）、Alex（延迟：67ms）、
      Notch（延迟：23ms）"
```

#### 6. get_errors
获取带有可选插件过滤的错误日志。

**示例：**
```
AI："显示WorldEdit插件的所有错误"
响应："从WorldEdit找到2个错误：[时间戳] 空指针异常..."
```

#### 7. get_warnings
获取带有可选插件过滤的警告日志。

**示例：**
```
AI："最近记录了哪些警告？"
响应："找到3个警告：[时间戳] [WARN] 无法跟上！服务器过载..."
```

## 系统要求

### Python MCP服务器
- Python 3.12或更高版本
- 依赖项（自动安装）：
  - `mcp[cli]` - 模型上下文协议实现
  - `httpx` - 异步HTTP客户端
  - `pydantic` - 数据验证

### Minecraft插件
- Minecraft服务器：Spigot或Paper 1.8.9或更高版本
- Java版本：
  - Java 8+用于Minecraft 1.8.9 - 1.16.5
  - Java 17+用于Minecraft 1.18.x - 1.21.x
- **注意：**不支持Minecraft 1.17.x

### 支持的Minecraft版本
- ✅ 1.8.9 - 1.16.5（旧版本）
- ❌ 1.17.x（不支持）
- ✅ 1.18.x - 1.21.x（现代版本）

## 故障排除

### 连接问题

**问题：**"连接失败"或"无法连接到插件"

**解决方案：**
1. 验证插件已安装并启用：在服务器控制台中使用`/plugins`
2. 检查插件HTTP服务器是否运行：在日志中查找"HTTP API server started"
3. 验证URL是否正确：默认为`http://localhost:8080`
4. 如果远程连接，检查防火墙设置
5. 确保插件配置和MCP服务器环境之间的API密钥匹配

### 安全考虑

- 使用强随机生成的API密钥（最少32个字符）
- 默认情况下，插件仅监听localhost（127.0.0.1）
- 仅在绝对必要时才暴露到外部网络
- 对于远程访问，使用带HTTPS的反向代理（nginx、Apache）
- 考虑使用VPN进行远程管理

## 文档

- **[MCP配置指南](MCP_CONFIGURATION.md)** - 详细的MCP服务器设置和配置
- **[插件配置指南](plugin/CONFIGURATION.md)** - 插件设置、安全和故障排除
- **[使用示例](EXAMPLES.md)** - 实用示例和常见用例
- **[安装指南](INSTALL.md)** - 分步安装说明

## 许可证

MIT

## 支持

如有问题、疑问或功能请求，请在项目仓库上提交issue。
