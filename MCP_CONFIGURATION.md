# MCP Configuration Guide

Configuration guide for the Minecraft Server MCP.

## Environment Variables

### Required

**MINECRAFT_PLUGIN_URL**
- URL of the Minecraft plugin HTTP API
- Format: `http://host:port`
- Example: `http://localhost:8080`

**MINECRAFT_API_KEY**
- API key for authentication
- Must match plugin's `config.yml`
- Minimum 32 characters recommended

### Optional

**MINECRAFT_REQUEST_TIMEOUT**
- HTTP request timeout in seconds
- Default: `10`

**MINECRAFT_LOG_LEVEL**
- Logging level: DEBUG, INFO, WARNING, ERROR
- Default: `INFO`

**MINECRAFT_LOG_FILE**
- Log file path
- Default: `smc-server.log`

## Configuration Methods

### 1. Environment Variables

**Linux/Mac:**
```bash
export MINECRAFT_PLUGIN_URL=http://localhost:8080
export MINECRAFT_API_KEY=your-api-key-here
export MINECRAFT_LOG_LEVEL=INFO
```

**Windows (PowerShell):**
```powershell
$env:MINECRAFT_PLUGIN_URL="http://localhost:8080"
$env:MINECRAFT_API_KEY="your-api-key-here"
$env:MINECRAFT_LOG_LEVEL="INFO"
```

**Windows (CMD):**
```cmd
set MINECRAFT_PLUGIN_URL=http://localhost:8080
set MINECRAFT_API_KEY=your-api-key-here
set MINECRAFT_LOG_LEVEL=INFO
```

### 2. MCP Client Configuration

**Claude Desktop (mcp.json):**
```json
{
  "mcpServers": {
    "minecraft": {
      "command": "python",
      "args": ["-m", "smc"],
      "env": {
        "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
        "MINECRAFT_API_KEY": "your-api-key-here",
        "MINECRAFT_LOG_LEVEL": "INFO"
      }
    }
  }
}
```

**Cline (.vscode/settings.json):**
```json
{
  "cline.mcpServers": {
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

**Continue (config.json):**
```json
{
  "mcpServers": [
    {
      "name": "minecraft",
      "command": "python",
      "args": ["-m", "smc"],
      "env": {
        "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
        "MINECRAFT_API_KEY": "your-api-key-here"
      }
    }
  ]
}
```

## Multi-Server Setup

To manage multiple Minecraft servers, create separate MCP server instances:

```json
{
  "mcpServers": {
    "minecraft-survival": {
      "command": "python",
      "args": ["-m", "smc"],
      "env": {
        "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
        "MINECRAFT_API_KEY": "survival-server-key"
      }
    },
    "minecraft-creative": {
      "command": "python",
      "args": ["-m", "smc"],
      "env": {
        "MINECRAFT_PLUGIN_URL": "http://localhost:8081",
        "MINECRAFT_API_KEY": "creative-server-key"
      }
    }
  }
}
```

## Remote Server Configuration

For remote servers, use the server's IP or domain:

```bash
export MINECRAFT_PLUGIN_URL=http://192.168.1.100:8080
export MINECRAFT_API_KEY=your-api-key-here
```

**Security Notes:**
- Use HTTPS for remote connections when possible
- Consider using SSH tunneling: `ssh -L 8080:localhost:8080 user@server`
- Use VPN for secure remote access
- Never expose the plugin API directly to the internet

## Troubleshooting

**Connection Refused:**
- Check plugin is running: `/plugins` in Minecraft console
- Verify URL and port are correct
- Check firewall settings

**Authentication Failed:**
- Verify API keys match exactly (case-sensitive)
- Check plugin config: `plugins/SMC/config.yml`
- Reload plugin after config changes

**Timeout Errors:**
- Increase `MINECRAFT_REQUEST_TIMEOUT`
- Check network latency
- Verify server is responsive

## Testing Configuration

Test your configuration:

```bash
# Set environment variables
export MINECRAFT_PLUGIN_URL=http://localhost:8080
export MINECRAFT_API_KEY=your-api-key-here

# Test connection
python -c "
from smc.client import PluginAPIClient
import asyncio

async def test():
    client = PluginAPIClient('http://localhost:8080', 'your-api-key-here')
    connected = await client.check_connection()
    print('Connected!' if connected else 'Failed to connect')
    await client.close()

asyncio.run(test())
"
```

## See Also

- [Installation Guide](INSTALL.md) - Installation instructions
- [Plugin Configuration](plugin/CONFIGURATION.md) - Plugin setup
- [Usage Examples](EXAMPLES.md) - Usage examples
