# Installation Guide

Quick installation guide for Minecraft Server MCP.

## Prerequisites

- **Python**: 3.12 or higher
- **Java**: 8+ (MC 1.8-1.16) or 17+ (MC 1.18-1.21)
- **Minecraft Server**: Spigot or Paper 1.8.9+ (excluding 1.17.x)

## Quick Install

### Option 1: Automated Setup

**Windows:**
```bash
quick-start.bat
```

**Linux/Mac:**
```bash
chmod +x quick-start.sh
./quick-start.sh
```

### Option 2: Manual Setup

#### 1. Build and Install Plugin

```bash
# Build plugin
cd plugin
mvn clean package

# Copy to server
cp target/SMC-Plugin.jar /path/to/minecraft/plugins/

# Restart Minecraft server
# Copy the auto-generated API key from console
```

#### 2. Install MCP Server

```bash
# Install dependencies
pip install -e .

# Configure
export MINECRAFT_PLUGIN_URL=http://localhost:8080
export MINECRAFT_API_KEY=your-api-key-from-step-1
```

#### 3. Configure MCP Client

Add to your MCP configuration file:

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

## Verification

Test the connection:

```bash
# Check plugin is loaded
# In Minecraft console: /plugins

# Test MCP server
python -c "import smc; print('OK')"
```

## Troubleshooting

**Plugin not loading:**
- Check Java version matches Minecraft version
- Verify plugin JAR is in plugins/ folder
- Check server logs for errors

**Connection failed:**
- Verify plugin is running: look for "HTTP API server started" in logs
- Check API keys match exactly
- Ensure URL is correct (default: http://localhost:8080)

**Permission denied:**
- Check file permissions on plugin JAR
- Verify Python has network access

## Next Steps

- See [MCP_CONFIGURATION.md](MCP_CONFIGURATION.md) for advanced configuration
- See [EXAMPLES.md](EXAMPLES.md) for usage examples
- See [plugin/CONFIGURATION.md](plugin/CONFIGURATION.md) for plugin settings

## Support

For issues, open an issue on the project repository.
