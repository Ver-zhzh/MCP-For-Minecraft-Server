# SMC Plugin

Minecraft Server MCP Plugin - HTTP API for server management.

## Building

```bash
cd plugin
mvn clean package
```

The compiled plugin will be in `target/SMC-Plugin.jar`.

## Installation

1. Copy `SMC-Plugin.jar` to your server's `plugins/` directory
2. Start or restart your Minecraft server
3. Edit `plugins/SMC/config.yml` to set your API key
4. Reload the plugin with `/reload confirm` or restart the server

## Configuration

The plugin creates a configuration file at `plugins/SMC/config.yml` on first run.

**Quick Configuration:**
1. Edit `plugins/SMC/config.yml`
2. Change the `api-key` to a secure random string (minimum 32 characters)
3. Adjust other settings as needed (see below)
4. Reload the plugin: `/reload confirm` or restart the server

**Default Configuration:**
```yaml
http:
  enabled: true
  host: "127.0.0.1"
  port: 8080
  api-key: "change-this-to-a-secure-random-key"

logging:
  buffer-size: 10000
  retention-hours: 24

commands:
  timeout-seconds: 30
  blacklist:
    - "stop"
    - "restart"
    - "reload"
```

For detailed configuration instructions, security best practices, and troubleshooting, see [CONFIGURATION.md](CONFIGURATION.md).

## Requirements

- Minecraft 1.8.9+ (Spigot/Paper)
- Java 8+

## Supported Versions

- 1.8.9 - 1.16.5 (Java 8/11)
- 1.18.x - 1.21.x (Java 17+)

Note: 1.17.x is not supported.
