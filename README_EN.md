# Minecraft Server MCP

A Model Context Protocol (MCP) server for managing Minecraft servers through AI assistants.

## Overview

This project enables AI assistants to interact with Minecraft servers (Spigot/Paper) through a standardized MCP interface. It consists of two components that work together:

1. **MCP Server** (Python) - Provides MCP tools for AI assistants to interact with
2. **Minecraft Plugin** (Java) - Runs on the Minecraft server and provides HTTP API endpoints

The system allows AI assistants to monitor server status, manage plugins, execute commands, query logs, and manage players - all through natural language interactions.

## Features

### Core Functionality
- **Server Status Monitoring** - Check connection status, server version, and online state
- **Plugin Management** - List all installed plugins with versions and enabled status
- **Command Execution** - Execute single or multiple server commands with output capture
- **Log Querying** - Retrieve server logs with time range and limit filtering
- **Player Management** - Get list of online players with detailed information
- **Error Log Filtering** - Query error logs with optional plugin filtering
- **Warning Log Filtering** - Query warning logs with optional plugin filtering

### Technical Features
- **Version Compatibility** - Supports Minecraft 1.8.9 through 1.21.x (excluding 1.17.x)
- **Secure Authentication** - API key-based authentication for all requests
- **Async Operations** - Non-blocking HTTP communication
- **Memory Efficient** - Circular buffer for log storage (configurable size)
- **Thread Safe** - Concurrent request handling with proper synchronization

## Installation

### Quick Start

For a quick setup, use the provided scripts:

**Windows:**
```bash
quick-start.bat
```

**Linux/Mac:**
```bash
./quick-start.sh
```

These scripts will build both components and provide instructions for deployment.

### Manual Installation

#### Step 1: Install the Minecraft Plugin

1. **Build the plugin:**
   ```bash
   cd plugin
   mvn clean package
   ```
   Or use the build script:
   - Windows: `plugin\build.bat`
   - Linux/Mac: `./plugin/build.sh`

2. **Install on your server:**
   - Copy `plugin/target/SMC-Plugin.jar` to your Minecraft server's `plugins/` directory
   - Start or restart your Minecraft server
   - The plugin will create a default config at `plugins/SMC/config.yml`

3. **Get the auto-generated API key:**
   - The plugin will automatically generate a secure API key on first run
   - Check the server console for the generated key
   - Look for a message like: `API Key: GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc`
   - Copy this key for use in the MCP server configuration

4. **Optional: Customize settings:**
   - Edit `plugins/SMC/config.yml` if you want to change other settings
   - Reload with `/reload confirm` or restart the server

#### Step 2: Install the Python MCP Server

1. **Install Python dependencies:**
   ```bash
   # Using uv (recommended)
   uv pip install -e .
   
   # Or using pip
   pip install -e .
   ```

2. **Configure environment variables:**
   ```bash
   # Linux/Mac
   export MINECRAFT_PLUGIN_URL=http://localhost:8080
   export MINECRAFT_API_KEY=your-secure-api-key-here
   
   # Windows (PowerShell)
   $env:MINECRAFT_PLUGIN_URL="http://localhost:8080"
   $env:MINECRAFT_API_KEY="your-secure-api-key-here"
   
   # Windows (CMD)
   set MINECRAFT_PLUGIN_URL=http://localhost:8080
   set MINECRAFT_API_KEY=your-secure-api-key-here
   ```

3. **Add to your MCP client configuration** (see Configuration section below)

## Configuration

### MCP Server

Configure via environment variables:

**Required:**
- `MINECRAFT_PLUGIN_URL` - URL of the Minecraft plugin HTTP API (e.g., `http://localhost:8080`)
- `MINECRAFT_API_KEY` - API key for authentication (must match plugin config)

**Optional:**
- `MINECRAFT_REQUEST_TIMEOUT` - HTTP request timeout in seconds (default: 10)
- `MINECRAFT_LOG_LEVEL` - Logging level: DEBUG, INFO, WARNING, ERROR (default: INFO)

### MCP Configuration Example

Add to your MCP settings file (e.g., `mcp.json`):

```json
{
  "mcpServers": {
    "minecraft": {
      "command": "python",
      "args": ["-m", "smc"],
      "env": {
        "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
        "MINECRAFT_API_KEY": "your-secure-api-key-here"
      }
    }
  }
}
```

### Minecraft Plugin

Edit `plugins/SMC/config.yml` on your Minecraft server:

```yaml
http:
  enabled: true
  host: "127.0.0.1"
  port: 8080
  api-key: "your-secure-api-key-here"

logging:
  buffer-size: 10000
  retention-hours: 24

commands:
  timeout-seconds: 30
  blacklist:
    - "stop"
    - "restart"
```

## Usage

Once configured, the MCP server provides the following tools to AI assistants:

### Available Tools

#### 1. status
Check server connection and get version information.

**Example:**
```
AI: "What's the status of my Minecraft server?"
Response: "The server is online, running Paper 1.20.4 (Minecraft 1.20.4)"
```

#### 2. plugins
List all installed plugins with versions and status.

**Example:**
```
AI: "Show me all installed plugins"
Response: "You have 5 plugins installed: WorldEdit (7.2.15, enabled), 
          EssentialsX (2.20.1, enabled), ..."
```

#### 3. send_command
Execute one or more server commands.

**Example:**
```
AI: "Give all players diamond swords"
Command executed: "give @a diamond_sword 1"
Response: "Command executed successfully. Gave 3 players diamond swords."
```

#### 4. get_logs
Retrieve server logs with optional filtering.

**Example:**
```
AI: "Show me the last 50 log entries"
Response: "Here are the last 50 log entries: [timestamp] [INFO] Server started..."
```

#### 5. player_list
Get list of online players.

**Example:**
```
AI: "Who is online right now?"
Response: "3 players are online: Steve (ping: 45ms), Alex (ping: 67ms), 
          Notch (ping: 23ms)"
```

#### 6. get_errors
Get error logs with optional plugin filtering.

**Example:**
```
AI: "Show me all errors from the WorldEdit plugin"
Response: "Found 2 errors from WorldEdit: [timestamp] NullPointerException..."
```

#### 7. get_warnings
Get warning logs with optional plugin filtering.

**Example:**
```
AI: "What warnings have been logged recently?"
Response: "Found 3 warnings: [timestamp] [WARN] Can't keep up! 
          Server overloaded..."
```

## Requirements

### Python MCP Server
- Python 3.12 or higher
- Dependencies (automatically installed):
  - `mcp[cli]` - Model Context Protocol implementation
  - `httpx` - Async HTTP client
  - `pydantic` - Data validation

### Minecraft Plugin
- Minecraft Server: Spigot or Paper 1.8.9 or higher
- Java Version:
  - Java 8+ for Minecraft 1.8.9 - 1.16.5
  - Java 17+ for Minecraft 1.18.x - 1.21.x
- **Note:** Minecraft 1.17.x is not supported

### Supported Minecraft Versions
- ✅ 1.8.9 - 1.16.5 (Legacy versions)
- ❌ 1.17.x (Not supported)
- ✅ 1.18.x - 1.21.x (Modern versions)

## Troubleshooting

### Connection Issues

**Problem:** "Connection failed" or "Unable to connect to plugin"

**Solutions:**
1. Verify the plugin is installed and enabled: `/plugins` in server console
2. Check the plugin HTTP server is running: Look for "HTTP API server started" in logs
3. Verify the URL is correct: Default is `http://localhost:8080`
4. Check firewall settings if connecting remotely
5. Ensure API keys match between plugin config and MCP server environment

### Security Considerations

- Use a strong, randomly generated API key (minimum 32 characters)
- By default, the plugin only listens on localhost (127.0.0.1)
- Only expose to external networks if absolutely necessary
- Use a reverse proxy (nginx, Apache) with HTTPS for remote access
- Consider using a VPN for remote management

## Documentation

- **[MCP Configuration Guide](MCP_CONFIGURATION.md)** - Detailed MCP server setup and configuration
- **[Plugin Configuration Guide](plugin/CONFIGURATION.md)** - Plugin setup, security, and troubleshooting
- **[Usage Examples](EXAMPLES.md)** - Practical examples and common use cases
- **[Installation Guide](INSTALL.md)** - Step-by-step installation instructions

## License

MIT

## Support

For issues, questions, or feature requests, please open an issue on the project repository.
