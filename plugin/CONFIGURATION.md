# SMC Plugin Configuration Guide

This guide explains how to configure the Minecraft Server MCP (SMC) plugin.

## Configuration File Location

After the plugin is installed and run for the first time, the configuration file will be created at:

```
plugins/SMC/config.yml
```

## Default Configuration

```yaml
# HTTP API Server Configuration
http:
  # Enable or disable the HTTP API server
  enabled: true
  
  # Host address to bind to
  # Use "127.0.0.1" for localhost only (recommended for security)
  # Use "0.0.0.0" to allow external connections (not recommended)
  host: "127.0.0.1"
  
  # Port number for the HTTP server
  port: 8080
  
  # API key for authentication (CHANGE THIS!)
  # Use a strong, randomly generated key (minimum 32 characters)
  api-key: "change-this-to-a-secure-random-key"

# Log Collection Configuration
logging:
  # Maximum number of log entries to keep in memory
  # Higher values use more memory but retain more history
  # Recommended: 5000-20000
  buffer-size: 10000
  
  # How long to retain logs in hours
  # Logs older than this will be automatically removed
  # Recommended: 12-48 hours
  retention-hours: 24

# Command Execution Configuration
commands:
  # Maximum time in seconds to wait for a command to complete
  # Commands taking longer will be terminated
  timeout-seconds: 30
  
  # List of commands that cannot be executed via the API
  # Add dangerous commands here to prevent accidental execution
  blacklist:
    - "stop"
    - "restart"
    - "reload"
```

## Configuration Options Explained

### HTTP Server Settings

#### `http.enabled`
- **Type:** Boolean (true/false)
- **Default:** `true`
- **Description:** Enable or disable the HTTP API server. If disabled, the plugin will load but not accept any API requests.
- **When to change:** Set to `false` if you want to temporarily disable the API without uninstalling the plugin.

#### `http.host`
- **Type:** String (IP address)
- **Default:** `"127.0.0.1"`
- **Description:** The network interface to bind the HTTP server to.
- **Options:**
  - `"127.0.0.1"` - Only accept connections from localhost (most secure)
  - `"0.0.0.0"` - Accept connections from any network interface (less secure)
  - Specific IP - Bind to a specific network interface
- **Security Note:** Using `"0.0.0.0"` exposes the API to your network. Only use this if you understand the security implications and have proper firewall rules in place.

#### `http.port`
- **Type:** Integer (1-65535)
- **Default:** `8080`
- **Description:** The TCP port number for the HTTP server.
- **When to change:** If port 8080 is already in use by another application, choose a different port (e.g., 8081, 9090).
- **Note:** If you change this, update the `MINECRAFT_PLUGIN_URL` environment variable for the MCP server.

#### `http.api-key`
- **Type:** String
- **Default:** `"change-this-to-a-secure-random-key"`
- **Description:** The API key required for authentication. All API requests must include this key in the `X-API-Key` header.
- **IMPORTANT:** You MUST change this from the default value before using the plugin in production.
- **Requirements:**
  - Minimum 32 characters recommended
  - Use random, unpredictable characters
  - Include letters, numbers, and special characters
  - Keep it secret and secure
- **Example:** `"7k9mP2nQ5rT8wX1zA4bC6dE9fH2jK5mN8pR1sU4vY7zA"`

### Logging Settings

#### `logging.buffer-size`
- **Type:** Integer (positive number)
- **Default:** `10000`
- **Description:** Maximum number of log entries to store in memory.
- **Memory Impact:** Each log entry uses approximately 200-500 bytes. 10,000 entries ≈ 2-5 MB.
- **Recommendations:**
  - Small servers (< 10 players): 5,000
  - Medium servers (10-50 players): 10,000
  - Large servers (50+ players): 20,000
- **Note:** When the buffer is full, the oldest entries are automatically removed.

#### `logging.retention-hours`
- **Type:** Integer (positive number)
- **Default:** `24`
- **Description:** How many hours to keep log entries before automatically removing them.
- **Recommendations:**
  - For troubleshooting: 24-48 hours
  - For minimal memory usage: 6-12 hours
  - For long-term monitoring: 48-72 hours
- **Note:** Logs are cleaned up periodically (every hour).

### Command Execution Settings

#### `commands.timeout-seconds`
- **Type:** Integer (positive number)
- **Default:** `30`
- **Description:** Maximum time in seconds to wait for a command to complete before timing out.
- **Recommendations:**
  - Fast commands (teleport, give): 10-15 seconds
  - Normal commands (most operations): 30 seconds
  - Slow commands (world generation): 60-120 seconds
- **Note:** Commands that timeout will return an error but may still complete on the server.

#### `commands.blacklist`
- **Type:** List of strings
- **Default:** `["stop", "restart", "reload"]`
- **Description:** Commands that cannot be executed through the API.
- **Purpose:** Prevent accidental or malicious execution of dangerous commands.
- **Recommended additions:**
  - `"op"` - Prevent granting operator permissions
  - `"deop"` - Prevent removing operator permissions
  - `"whitelist"` - Prevent whitelist modifications
  - `"ban"` - Prevent banning players (if you want to control this manually)
  - `"pardon"` - Prevent unbanning players
- **Example:**
  ```yaml
  blacklist:
    - "stop"
    - "restart"
    - "reload"
    - "op"
    - "deop"
    - "whitelist"
  ```

## API Key Setup Guide

### 🎉 Automatic Generation (Recommended)

**The plugin now automatically generates a secure API key on first run!**

#### Step 1: Install and Start the Plugin

1. Copy the plugin JAR to your server's `plugins/` directory
2. Start or restart your Minecraft server
3. The plugin will automatically generate a secure API key

#### Step 2: Get the Generated Key

Check your server console for output like this:

```
[SMC] =================================
[SMC] Generated new API key!
[SMC] API Key: GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc
[SMC]
[SMC] Please use this key in your MCP server configuration:
[SMC] MINECRAFT_API_KEY=GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc
[SMC] =================================
```

#### Step 3: Copy the Key

Copy the generated API key (e.g., `GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc`)

#### Step 4: Configure MCP Server

Use the key in your MCP server configuration:

**Environment Variable:**
```bash
# Linux/Mac
export MINECRAFT_API_KEY="GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc"

# Windows (PowerShell)
$env:MINECRAFT_API_KEY="GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc"
```

**MCP Configuration File (mcp.json):**
```json
{
  "mcpServers": {
    "minecraft": {
      "env": {
        "MINECRAFT_API_KEY": "GxNMYlwXIg0ujorJHsALSuRSR48ZPYNc"
      }
    }
  }
}
```

#### Step 5: Done!

That's it! The plugin is ready to use.

---

### 🔧 Manual Generation (Optional)

If you prefer to generate your own API key:

**Option A: Using PowerShell (Windows)**
```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
```

**Option B: Using Python**
```python
import secrets
print(secrets.token_urlsafe(32))
```

**Option C: Using OpenSSL (Linux/Mac)**
```bash
openssl rand -base64 32
```

Then manually edit `plugins/SMC/config.yml`:
```yaml
http:
  api-key: "YOUR_CUSTOM_KEY_HERE"
```

And reload the plugin: `/reload confirm`

---

### 🔄 Regenerating the API Key

If you need to regenerate the API key:

1. Open `plugins/SMC/config.yml`
2. Change the `api-key` back to the default:
   ```yaml
   api-key: "change-this-to-a-secure-random-key"
   ```
3. Reload the plugin: `/reload confirm`
4. A new key will be automatically generated and displayed in the console

---

### 📝 Finding Your Current API Key

If you forgot your API key, you can find it in:
```
plugins/SMC/config.yml
```

Look for the `http.api-key` field.

## Security Best Practices

### API Key Security

1. **Use Strong Keys**
   - Minimum 32 characters
   - Random and unpredictable
   - Include mixed case, numbers, and special characters

2. **Keep Keys Secret**
   - Never commit API keys to version control
   - Don't share keys in chat or email
   - Use environment variables or secure vaults
   - Rotate keys periodically (every 3-6 months)

3. **Different Keys for Different Environments**
   - Use separate keys for development, staging, and production
   - Never use production keys in development

### Network Security

1. **Localhost Only (Recommended)**
   ```yaml
   http:
     host: "127.0.0.1"
   ```
   - Only allows connections from the same machine
   - Most secure option
   - Requires MCP server to run on the same machine

2. **Remote Access (Advanced)**
   If you need remote access:
   - Use a VPN to connect to the server's network
   - Or use SSH tunneling:
     ```bash
     ssh -L 8080:localhost:8080 user@minecraft-server
     ```
   - Or use a reverse proxy with HTTPS (nginx, Apache)
   - Never expose directly to the internet without HTTPS

3. **Firewall Configuration**
   If using `host: "0.0.0.0"`:
   - Configure firewall to only allow specific IP addresses
   - Block port 8080 from public internet
   - Use iptables (Linux) or Windows Firewall rules

### Command Security

1. **Maintain Command Blacklist**
   - Regularly review and update the blacklist
   - Add any commands that could disrupt service
   - Consider the impact of each command

2. **Monitor Command Execution**
   - Regularly check logs for executed commands
   - Look for suspicious or unexpected commands
   - Set up alerts for critical commands

3. **Principle of Least Privilege**
   - Only give API access to trusted systems
   - Limit what commands can be executed
   - Consider creating a separate user with limited permissions

### File Security

1. **Protect Configuration Files**
   ```bash
   # Linux/Mac
   chmod 600 plugins/SMC/config.yml
   chown minecraft:minecraft plugins/SMC/config.yml
   ```

2. **Regular Backups**
   - Back up configuration before changes
   - Include config in regular server backups
   - Test restoration procedures

3. **Audit Trail**
   - Keep logs of configuration changes
   - Document who made changes and when
   - Review logs regularly

## Common Configuration Scenarios

### Scenario 1: Local Development

```yaml
http:
  enabled: true
  host: "127.0.0.1"
  port: 8080
  api-key: "dev-key-not-for-production"

logging:
  buffer-size: 5000
  retention-hours: 12

commands:
  timeout-seconds: 30
  blacklist:
    - "stop"
```

### Scenario 2: Production Server (Localhost Only)

```yaml
http:
  enabled: true
  host: "127.0.0.1"
  port: 8080
  api-key: "7k9mP2nQ5rT8wX1zA4bC6dE9fH2jK5mN8pR1sU4vY7zA"

logging:
  buffer-size: 15000
  retention-hours: 48

commands:
  timeout-seconds: 30
  blacklist:
    - "stop"
    - "restart"
    - "reload"
    - "op"
    - "deop"
    - "whitelist"
    - "ban"
    - "pardon"
```

### Scenario 3: Remote Access (Advanced)

```yaml
http:
  enabled: true
  host: "0.0.0.0"  # Exposed to network
  port: 8080
  api-key: "VERY-LONG-AND-SECURE-RANDOM-KEY-HERE-AT-LEAST-64-CHARS"

logging:
  buffer-size: 10000
  retention-hours: 24

commands:
  timeout-seconds: 45
  blacklist:
    - "stop"
    - "restart"
    - "reload"
    - "op"
    - "deop"
    - "whitelist"
    - "ban"
    - "pardon"
    - "kick"
```

**Note:** For remote access, also configure firewall rules and consider using a reverse proxy with HTTPS.

### Scenario 4: High-Traffic Server

```yaml
http:
  enabled: true
  host: "127.0.0.1"
  port: 8080
  api-key: "secure-production-key-here"

logging:
  buffer-size: 20000  # More logs for busy server
  retention-hours: 24

commands:
  timeout-seconds: 60  # Longer timeout for busy server
  blacklist:
    - "stop"
    - "restart"
    - "reload"
```

## Troubleshooting Configuration Issues

### Plugin Won't Start

**Symptom:** Plugin shows errors on startup or doesn't load

**Possible Causes:**
1. Invalid YAML syntax in config.yml
2. Port already in use
3. Invalid configuration values

**Solutions:**
1. Validate YAML syntax using an online YAML validator
2. Check for tabs (use spaces only)
3. Try changing the port number
4. Check server logs for specific error messages
5. Delete config.yml and let it regenerate with defaults

### API Connection Fails

**Symptom:** MCP server can't connect to plugin

**Possible Causes:**
1. Plugin not running
2. Wrong URL or port
3. API key mismatch
4. Firewall blocking connection

**Solutions:**
1. Verify plugin is enabled: `/plugins` command
2. Check plugin logs for "HTTP API server started on..."
3. Verify URL matches: `http://localhost:8080` (or your configured port)
4. Ensure API keys match exactly (case-sensitive)
5. Check firewall settings
6. Try `curl http://localhost:8080/api/status -H "X-API-Key: YOUR_KEY"`

### High Memory Usage

**Symptom:** Server using too much memory

**Possible Causes:**
1. Log buffer too large
2. Too many logs being generated

**Solutions:**
1. Reduce `buffer-size` (try 5000)
2. Reduce `retention-hours` (try 12)
3. Restart server to clear buffer
4. Check for plugins generating excessive logs

### Commands Timing Out

**Symptom:** Commands return timeout errors

**Possible Causes:**
1. Server is lagging
2. Command takes longer than timeout
3. Command is stuck

**Solutions:**
1. Increase `timeout-seconds` (try 60)
2. Check server TPS
3. Optimize server performance
4. Try executing command manually to verify it works

## Configuration Validation

After making changes to the configuration:

1. **Validate YAML Syntax**
   - Use an online YAML validator
   - Check for proper indentation (2 spaces)
   - Ensure no tabs are used

2. **Test the Configuration**
   - Reload the plugin: `/reload confirm`
   - Check server logs for errors
   - Test API connection: `python test_manual_server.py`

3. **Verify Security**
   - Ensure API key is strong and unique
   - Verify host binding is appropriate
   - Review command blacklist

4. **Monitor Performance**
   - Check memory usage after changes
   - Monitor log collection performance
   - Verify command execution times

## Getting Help

If you encounter issues with configuration:

1. Check the server logs in `logs/latest.log`
2. Check the plugin logs (if separate logging is configured)
3. Verify your configuration against the examples in this guide
4. Test with the default configuration
5. Open an issue on the project repository with:
   - Your configuration (with API key redacted)
   - Error messages from logs
   - Minecraft version and server type
   - Java version
