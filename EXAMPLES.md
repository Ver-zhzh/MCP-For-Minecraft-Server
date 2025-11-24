# Usage Examples

This document provides practical examples of using the Minecraft Server MCP with AI assistants.

## Table of Contents

1. [Basic Commands](#basic-commands)
2. [Log Querying](#log-querying)
3. [Error Handling](#error-handling)
4. [Server Management](#server-management)
5. [Player Management](#player-management)
6. [Troubleshooting Scenarios](#troubleshooting-scenarios)
7. [Advanced Usage](#advanced-usage)

## Basic Commands

### Example 1: Check Server Status

**User Request:**
```
"Is my Minecraft server running?"
```

**AI Assistant Action:**
- Calls `status` tool

**Expected Response:**
```
Yes, your Minecraft server is online and running Paper 1.20.4 (Minecraft version 1.20.4).
```

**Tool Output:**
```json
{
  "connected": true,
  "server_version": "Paper 1.20.4",
  "minecraft_version": "1.20.4",
  "online": true
}
```

---

### Example 2: List Installed Plugins

**User Request:**
```
"What plugins are installed on my server?"
```

**AI Assistant Action:**
- Calls `plugins` tool

**Expected Response:**
```
You have 6 plugins installed:
1. WorldEdit v7.2.15 (enabled) - by sk89q, wizjany
2. EssentialsX v2.20.1 (enabled) - by Essentials Team
3. Vault v1.7.3 (enabled) - by cereal, Sleaker
4. LuckPerms v5.4.102 (enabled) - by Luck
5. WorldGuard v7.0.9 (enabled) - by sk89q, wizjany
6. CoreProtect v22.2 (enabled) - by Intelli
```

**Tool Output:**
```json
{
  "plugins": [
    {
      "name": "WorldEdit",
      "version": "7.2.15",
      "enabled": true,
      "authors": ["sk89q", "wizjany"]
    },
    {
      "name": "EssentialsX",
      "version": "2.20.1",
      "enabled": true,
      "authors": ["Essentials Team"]
    }
    // ... more plugins
  ]
}
```

---

### Example 3: Execute a Simple Command

**User Request:**
```
"Set the time to day"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "time set day"`

**Expected Response:**
```
Command executed successfully. The time has been set to day.
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "time set day",
      "output": "Set the time to 1000",
      "success": true
    }
  ]
}
```

---

### Example 4: Execute Multiple Commands

**User Request:**
```
"Set the time to day and clear the weather"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: ["time set day", "weather clear"]`

**Expected Response:**
```
Both commands executed successfully:
1. Set the time to day
2. Cleared the weather
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "time set day",
      "output": "Set the time to 1000",
      "success": true
    },
    {
      "command": "weather clear",
      "output": "Changed weather to clear",
      "success": true
    }
  ]
}
```

---

### Example 5: Give Items to Players

**User Request:**
```
"Give all online players a diamond sword"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "give @a diamond_sword 1"`

**Expected Response:**
```
Gave 1 diamond sword to 4 players: Steve, Alex, Notch, and Herobrine.
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "give @a diamond_sword 1",
      "output": "Gave 1 [Diamond Sword] to Steve\nGave 1 [Diamond Sword] to Alex\nGave 1 [Diamond Sword] to Notch\nGave 1 [Diamond Sword] to Herobrine",
      "success": true
    }
  ]
}
```

## Log Querying

### Example 6: Get Recent Logs

**User Request:**
```
"Show me the last 20 log entries"
```

**AI Assistant Action:**
- Calls `get_logs` tool with `limit: 20`

**Expected Response:**
```
Here are the last 20 log entries:

[14:23:45] [INFO] Steve joined the game
[14:23:50] [INFO] <Steve> Hello everyone!
[14:24:01] [INFO] Alex joined the game
[14:24:05] [INFO] <Alex> Hi Steve!
[14:24:15] [WARN] Can't keep up! Is the server overloaded?
[14:24:30] [INFO] Saving chunks for level 'ServerLevel[world]'
...
```

**Tool Output:**
```json
{
  "logs": [
    {
      "timestamp": "2024-01-15T14:23:45Z",
      "level": "INFO",
      "message": "Steve joined the game",
      "logger": "net.minecraft.server.players.PlayerList"
    },
    {
      "timestamp": "2024-01-15T14:23:50Z",
      "level": "INFO",
      "message": "<Steve> Hello everyone!",
      "logger": "net.minecraft.server.MinecraftServer"
    }
    // ... more logs
  ]
}
```

---

### Example 7: Get Logs from a Time Range

**User Request:**
```
"What happened on the server between 2pm and 3pm today?"
```

**AI Assistant Action:**
- Calls `get_logs` tool with `start_time: "2024-01-15T14:00:00Z"` and `end_time: "2024-01-15T15:00:00Z"`

**Expected Response:**
```
Found 156 log entries between 2pm and 3pm:

Key events:
- 5 players joined the game
- 2 players left the game
- 3 warnings about server performance
- 1 error from WorldEdit plugin
- Multiple chat messages
- Regular autosave operations
```

**Tool Output:**
```json
{
  "logs": [
    // Array of 156 log entries within the time range
  ]
}
```

---

### Example 8: Get Limited Logs

**User Request:**
```
"Show me the 5 most recent log entries"
```

**AI Assistant Action:**
- Calls `get_logs` tool with `limit: 5`

**Expected Response:**
```
Here are the 5 most recent log entries:

1. [14:30:15] [INFO] Saving chunks for level 'ServerLevel[world]'
2. [14:30:10] [INFO] <Alex> Thanks!
3. [14:30:05] [INFO] <Steve> You're welcome
4. [14:30:00] [INFO] Steve gave Alex 1 diamond
5. [14:29:55] [INFO] <Alex> Can I have a diamond?
```

## Error Handling

### Example 9: Get All Error Logs

**User Request:**
```
"Show me all recent errors"
```

**AI Assistant Action:**
- Calls `get_errors` tool

**Expected Response:**
```
Found 3 errors in the logs:

1. [14:15:23] [ERROR] [WorldEdit] NullPointerException in AsyncCommandBuilder
   at com.sk89q.worldedit.command.AsyncCommandBuilder.build()
   at com.sk89q.worldedit.command.CommandManager.execute()
   
2. [13:45:12] [ERROR] [Citizens] Failed to load NPC data
   java.io.FileNotFoundException: plugins/Citizens/saves/npc-123.yml
   
3. [12:30:45] [ERROR] [Server] Exception in thread "Server thread"
   java.lang.OutOfMemoryError: Java heap space
```

**Tool Output:**
```json
{
  "errors": [
    {
      "timestamp": "2024-01-15T14:15:23Z",
      "plugin": "WorldEdit",
      "message": "NullPointerException in AsyncCommandBuilder",
      "stacktrace": "at com.sk89q.worldedit.command.AsyncCommandBuilder.build()..."
    },
    {
      "timestamp": "2024-01-15T13:45:12Z",
      "plugin": "Citizens",
      "message": "Failed to load NPC data",
      "stacktrace": "java.io.FileNotFoundException: plugins/Citizens/saves/npc-123.yml"
    },
    {
      "timestamp": "2024-01-15T12:30:45Z",
      "plugin": null,
      "message": "Exception in thread \"Server thread\"",
      "stacktrace": "java.lang.OutOfMemoryError: Java heap space"
    }
  ]
}
```

---

### Example 10: Get Errors from Specific Plugin

**User Request:**
```
"Show me errors from the WorldEdit plugin"
```

**AI Assistant Action:**
- Calls `get_errors` tool with `plugin: "WorldEdit"`

**Expected Response:**
```
Found 2 errors from WorldEdit:

1. [14:15:23] [ERROR] NullPointerException in AsyncCommandBuilder
   This occurred when executing a //copy command
   
2. [11:20:15] [ERROR] Region selection is too large
   Maximum size is 1000000 blocks
```

**Tool Output:**
```json
{
  "errors": [
    {
      "timestamp": "2024-01-15T14:15:23Z",
      "plugin": "WorldEdit",
      "message": "NullPointerException in AsyncCommandBuilder",
      "stacktrace": "..."
    },
    {
      "timestamp": "2024-01-15T11:20:15Z",
      "plugin": "WorldEdit",
      "message": "Region selection is too large",
      "stacktrace": null
    }
  ]
}
```

---

### Example 11: Get Limited Error Logs

**User Request:**
```
"Show me the 3 most recent errors"
```

**AI Assistant Action:**
- Calls `get_errors` tool with `limit: 3`

**Expected Response:**
```
Here are the 3 most recent errors:

1. [14:15:23] [ERROR] [WorldEdit] NullPointerException in AsyncCommandBuilder
2. [13:45:12] [ERROR] [Citizens] Failed to load NPC data
3. [12:30:45] [ERROR] [Server] OutOfMemoryError: Java heap space
```

---

### Example 12: Get Warning Logs

**User Request:**
```
"What warnings have been logged recently?"
```

**AI Assistant Action:**
- Calls `get_warnings` tool

**Expected Response:**
```
Found 5 warnings:

1. [14:24:15] [WARN] Can't keep up! Is the server overloaded? Running 2500ms behind
2. [14:10:30] [WARN] [Citizens] Using deprecated API method
3. [13:55:20] [WARN] [Dynmap] Missing configuration value: render-triggers
4. [13:30:10] [WARN] Can't keep up! Running 1800ms behind
5. [12:45:00] [WARN] [LuckPerms] Player data migration recommended
```

**Tool Output:**
```json
{
  "warnings": [
    {
      "timestamp": "2024-01-15T14:24:15Z",
      "plugin": null,
      "message": "Can't keep up! Is the server overloaded? Running 2500ms behind"
    },
    {
      "timestamp": "2024-01-15T14:10:30Z",
      "plugin": "Citizens",
      "message": "Using deprecated API method"
    }
    // ... more warnings
  ]
}
```

---

### Example 13: Get Warnings from Specific Plugin

**User Request:**
```
"Show me warnings from the Citizens plugin"
```

**AI Assistant Action:**
- Calls `get_warnings` tool with `plugin: "Citizens"`

**Expected Response:**
```
Found 1 warning from Citizens:

[14:10:30] [WARN] Using deprecated API method
This API will be removed in the next major version. Please update Citizens.
```

## Server Management

### Example 14: Check Online Players

**User Request:**
```
"Who is online right now?"
```

**AI Assistant Action:**
- Calls `player_list` tool

**Expected Response:**
```
There are currently 4 players online:

1. Steve (ping: 45ms)
2. Alex (ping: 67ms)
3. Notch (ping: 23ms)
4. Herobrine (ping: 234ms)
```

**Tool Output:**
```json
{
  "count": 4,
  "players": [
    {
      "name": "Steve",
      "uuid": "069a79f4-44e9-4726-a5be-fca90e38aaf5",
      "ping": 45
    },
    {
      "name": "Alex",
      "uuid": "853c80ef-3c37-49fd-aa49-938b674adae6",
      "ping": 67
    },
    {
      "name": "Notch",
      "uuid": "069a79f4-44e9-4726-a5be-fca90e38aaf6",
      "ping": 23
    },
    {
      "name": "Herobrine",
      "uuid": "853c80ef-3c37-49fd-aa49-938b674adae7",
      "ping": 234
    }
  ]
}
```

---

### Example 15: Check Player Count

**User Request:**
```
"How many players are on the server?"
```

**AI Assistant Action:**
- Calls `player_list` tool

**Expected Response:**
```
There are currently 4 players online.
```

---

### Example 16: Teleport Player

**User Request:**
```
"Teleport Steve to Alex"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "tp Steve Alex"`

**Expected Response:**
```
Teleported Steve to Alex successfully.
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "tp Steve Alex",
      "output": "Teleported Steve to Alex",
      "success": true
    }
  ]
}
```

---

### Example 17: Change Game Mode

**User Request:**
```
"Set Alex to creative mode"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "gamemode creative Alex"`

**Expected Response:**
```
Set Alex's game mode to Creative Mode.
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "gamemode creative Alex",
      "output": "Set Alex's game mode to Creative Mode",
      "success": true
    }
  ]
}
```

## Player Management

### Example 18: Kick Player

**User Request:**
```
"Kick Herobrine for griefing"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "kick Herobrine Griefing is not allowed"`

**Expected Response:**
```
Kicked Herobrine from the server. Reason: Griefing is not allowed
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "kick Herobrine Griefing is not allowed",
      "output": "Kicked Herobrine: Griefing is not allowed",
      "success": true
    }
  ]
}
```

---

### Example 19: Send Message to Player

**User Request:**
```
"Tell Steve that the event starts in 5 minutes"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "tell Steve The event starts in 5 minutes!"`

**Expected Response:**
```
Message sent to Steve: "The event starts in 5 minutes!"
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "tell Steve The event starts in 5 minutes!",
      "output": "Whispered to Steve: The event starts in 5 minutes!",
      "success": true
    }
  ]
}
```

---

### Example 20: Broadcast Message

**User Request:**
```
"Announce that the server will restart in 10 minutes"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "say Server will restart in 10 minutes. Please save your progress!"`

**Expected Response:**
```
Broadcasted: "Server will restart in 10 minutes. Please save your progress!"
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "say Server will restart in 10 minutes. Please save your progress!",
      "output": "[Server] Server will restart in 10 minutes. Please save your progress!",
      "success": true
    }
  ]
}
```

## Troubleshooting Scenarios

### Example 21: Diagnose Server Lag

**User Request:**
```
"The server seems laggy. Can you help me figure out why?"
```

**AI Assistant Actions:**
1. Calls `get_warnings` tool
2. Calls `get_errors` tool
3. Calls `player_list` tool
4. Calls `plugins` tool

**Expected Response:**
```
I found several issues that might be causing lag:

1. Performance Warnings:
   - 3 "Can't keep up!" warnings in the last hour
   - Server is running 2500ms behind schedule

2. Errors:
   - 1 OutOfMemoryError at 12:30 PM
   - This suggests the server is running out of memory

3. Current Load:
   - 15 players online (might be high for your server specs)
   - All plugins are enabled and running

Recommendations:
- Increase server memory allocation
- Check for memory leaks in plugins
- Consider reducing view distance
- Monitor entity counts in loaded chunks
```

---

### Example 22: Investigate Plugin Error

**User Request:**
```
"WorldEdit is giving errors. What's wrong?"
```

**AI Assistant Actions:**
1. Calls `get_errors` tool with `plugin: "WorldEdit"`
2. Calls `plugins` tool

**Expected Response:**
```
I found 2 errors from WorldEdit:

1. NullPointerException in AsyncCommandBuilder (14:15:23)
   - This occurred when executing a //copy command
   - Likely a bug in WorldEdit

2. Region selection is too large (11:20:15)
   - Maximum size is 1,000,000 blocks
   - User tried to select a region that was too big

WorldEdit is currently enabled and running version 7.2.15.

Recommendations:
- Update WorldEdit to the latest version
- Report the NullPointerException to WorldEdit developers
- Inform users about the region size limit
```

---

### Example 23: Check for Recent Crashes

**User Request:**
```
"Did the server crash recently?"
```

**AI Assistant Actions:**
1. Calls `get_errors` tool
2. Calls `get_logs` tool with recent time range

**Expected Response:**
```
Yes, I found evidence of a crash at 12:30:45 PM:

Error: OutOfMemoryError: Java heap space

This indicates the server ran out of memory. The server appears to have restarted automatically.

Recent activity after restart:
- Server started at 12:35:00 PM
- 8 players reconnected
- All plugins loaded successfully

Recommendation: Increase the server's memory allocation to prevent future crashes.
```

---

### Example 24: Monitor Player Activity

**User Request:**
```
"What have players been doing in the last hour?"
```

**AI Assistant Action:**
- Calls `get_logs` tool with time range for last hour

**Expected Response:**
```
Player activity in the last hour:

Joins/Leaves:
- 5 players joined
- 2 players left
- Currently 4 players online

Chat Activity:
- 47 chat messages
- Most active: Steve (15 messages), Alex (12 messages)

Commands Used:
- /tp used 8 times
- /give used 3 times
- /gamemode used 2 times

Other Events:
- 2 autosaves completed
- 1 player died (fell from a high place)
- 3 achievements earned
```

## Advanced Usage

### Example 25: Complex Multi-Step Operation

**User Request:**
```
"Prepare the server for a building event: set time to day, clear weather, set all players to creative mode, and announce the event"
```

**AI Assistant Actions:**
1. Calls `player_list` tool to get player names
2. Calls `send_command` tool with multiple commands

**Commands:**
```json
{
  "commands": [
    "time set day",
    "weather clear",
    "gamemode creative @a",
    "say Building event is starting now! You are now in creative mode."
  ]
}
```

**Expected Response:**
```
Server prepared for building event:
✓ Set time to day
✓ Cleared weather
✓ Set 4 players to creative mode
✓ Announced event to all players

Everything is ready!
```

---

### Example 26: Automated Backup Verification

**User Request:**
```
"Did the automatic backup run successfully?"
```

**AI Assistant Action:**
- Calls `get_logs` tool with search for backup-related messages

**Expected Response:**
```
Yes, the automatic backup completed successfully:

[03:00:00] [INFO] Starting automatic backup...
[03:00:05] [INFO] Saving chunks for level 'ServerLevel[world]'
[03:00:10] [INFO] Saving chunks for level 'ServerLevel[world_nether]'
[03:00:12] [INFO] Saving chunks for level 'ServerLevel[world_the_end]'
[03:00:15] [INFO] Backup completed successfully
[03:00:15] [INFO] Backup size: 2.4 GB

The backup ran at 3:00 AM as scheduled and completed in 15 seconds.
```

---

### Example 27: Performance Monitoring

**User Request:**
```
"Give me a performance report for the last 24 hours"
```

**AI Assistant Actions:**
1. Calls `get_warnings` tool
2. Calls `get_errors` tool
3. Calls `get_logs` tool

**Expected Response:**
```
Performance Report (Last 24 Hours):

Warnings: 12 total
- 8 "Can't keep up!" warnings (server lag)
- 3 deprecated API warnings
- 1 configuration warning

Errors: 5 total
- 2 plugin errors (WorldEdit, Citizens)
- 2 network timeout errors
- 1 memory error (OutOfMemoryError at 12:30 PM)

Critical Issues:
- Memory crash at 12:30 PM (server restarted)
- Frequent lag warnings (8 occurrences)

Recommendations:
1. Increase server memory allocation
2. Update plugins with deprecated API usage
3. Investigate cause of frequent lag
4. Monitor memory usage more closely
```

---

### Example 28: Batch Player Management

**User Request:**
```
"Give all online players full diamond armor and tools"
```

**AI Assistant Action:**
- Calls `send_command` tool with multiple give commands

**Commands:**
```json
{
  "commands": [
    "give @a diamond_helmet 1",
    "give @a diamond_chestplate 1",
    "give @a diamond_leggings 1",
    "give @a diamond_boots 1",
    "give @a diamond_sword 1",
    "give @a diamond_pickaxe 1",
    "give @a diamond_axe 1",
    "give @a diamond_shovel 1"
  ]
}
```

**Expected Response:**
```
Gave full diamond armor and tools to 4 players:
✓ Diamond helmet
✓ Diamond chestplate
✓ Diamond leggings
✓ Diamond boots
✓ Diamond sword
✓ Diamond pickaxe
✓ Diamond axe
✓ Diamond shovel

All players are now equipped!
```

---

### Example 29: Error Recovery

**User Request:**
```
"A player reported they lost their items. Can you check what happened?"
```

**AI Assistant Actions:**
1. Calls `get_logs` tool with player name filter
2. Calls `get_errors` tool

**Expected Response:**
```
I found what happened to Steve's items:

[14:25:30] [INFO] Steve fell from a high place
[14:25:30] [INFO] Steve died at coordinates X: 125, Y: 85, Z: -340
[14:25:35] [INFO] Steve respawned

Steve died from fall damage at coordinates X: 125, Y: 85, Z: -340.
Their items should be at that location unless they despawned (items despawn after 5 minutes).

Would you like me to:
1. Teleport Steve to their death location?
2. Give Steve replacement items?
3. Check if the items are still there?
```

---

### Example 30: Scheduled Maintenance Check

**User Request:**
```
"Run a pre-maintenance check on the server"
```

**AI Assistant Actions:**
1. Calls `status` tool
2. Calls `plugins` tool
3. Calls `player_list` tool
4. Calls `get_errors` tool
5. Calls `get_warnings` tool

**Expected Response:**
```
Pre-Maintenance Check Report:

Server Status: ✓ Online
- Running Paper 1.20.4
- Uptime: 15 hours 32 minutes

Plugins: ✓ All operational
- 6 plugins installed
- All enabled and running
- No disabled plugins

Players: ⚠ 4 players online
- Steve, Alex, Notch, Herobrine
- Recommend notifying players before maintenance

Recent Errors: ⚠ 2 found
- 1 WorldEdit error (non-critical)
- 1 Citizens error (non-critical)

Recent Warnings: ⚠ 5 found
- 3 performance warnings
- 2 deprecated API warnings

Recommendations:
1. Notify online players about upcoming maintenance
2. Consider fixing the 2 plugin errors before maintenance
3. Server is otherwise ready for maintenance
```

## Error Handling Examples

### Example 31: Command Fails

**User Request:**
```
"Give Steve a flying_carpet"
```

**AI Assistant Action:**
- Calls `send_command` tool with `commands: "give Steve flying_carpet 1"`

**Expected Response:**
```
The command failed. Error: Unknown item 'flying_carpet'

'flying_carpet' is not a valid Minecraft item. Did you mean:
- carpet
- elytra (for flying)
```

**Tool Output:**
```json
{
  "results": [
    {
      "command": "give Steve flying_carpet 1",
      "output": "Unknown item 'minecraft:flying_carpet'",
      "success": false,
      "error": "Unknown item 'minecraft:flying_carpet'"
    }
  ]
}
```

---

### Example 32: Connection Lost

**User Request:**
```
"What's the server status?"
```

**AI Assistant Action:**
- Calls `status` tool
- Connection fails

**Expected Response:**
```
I'm unable to connect to the Minecraft server. 

Error: Connection refused at http://localhost:8080

Possible causes:
1. The Minecraft server is offline
2. The SMC plugin is not running
3. The HTTP API server is disabled
4. Network connectivity issues

Please check:
- Is the Minecraft server running?
- Is the SMC plugin enabled? (use /plugins command)
- Check the plugin configuration
```

**Tool Output:**
```json
{
  "connected": false,
  "error": "Connection refused"
}
```

---

### Example 33: Authentication Fails

**User Request:**
```
"List the plugins"
```

**AI Assistant Action:**
- Calls `plugins` tool
- Authentication fails

**Expected Response:**
```
Authentication failed. The API key is incorrect or missing.

Please check:
1. The API key in the plugin config (plugins/SMC/config.yml)
2. The API key in the MCP server configuration
3. Ensure both keys match exactly (case-sensitive)

After updating the configuration, reload the plugin with /reload confirm
```

**Tool Output:**
```json
{
  "error": "401 Unauthorized: Invalid API key"
}
```

## Tips for AI Assistants

When using these tools:

1. **Always check status first** when troubleshooting
2. **Combine multiple tools** for comprehensive analysis
3. **Filter logs appropriately** to find relevant information
4. **Provide context** when reporting errors to users
5. **Suggest solutions** based on error patterns
6. **Verify commands** before execution
7. **Handle errors gracefully** and explain what went wrong
8. **Use time ranges** when investigating specific incidents
9. **Check player list** before player-specific commands
10. **Monitor performance** by checking warnings regularly

## Common Patterns

### Investigation Pattern
1. Check status
2. Get recent errors
3. Get recent warnings
4. Get relevant logs
5. Analyze and report

### Command Execution Pattern
1. Verify server is online
2. Check if command is valid
3. Execute command
4. Verify success
5. Report result

### Player Management Pattern
1. Get player list
2. Verify player is online
3. Execute player-specific command
4. Confirm action
5. Report result

### Troubleshooting Pattern
1. Identify the problem
2. Check relevant logs
3. Check for errors/warnings
4. Analyze patterns
5. Suggest solutions
6. Verify fix if applicable
