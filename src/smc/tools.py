"""MCP tool implementations."""

from typing import Any, Dict, List, Optional, Union
from datetime import datetime
import logging

from .client import PluginAPIClient
from .config import ServerConfig
from .connection_manager import ConnectionManager

logger = logging.getLogger(__name__)

# Global connection manager instance
_connection_manager: Optional[ConnectionManager] = None


def get_connection_manager() -> ConnectionManager:
    """Get or create the global connection manager."""
    global _connection_manager
    if _connection_manager is None:
        _connection_manager = ConnectionManager()
    return _connection_manager


async def status_tool(api_key: str) -> Dict[str, Any]:
    """Get Minecraft server status."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "connected": False,
            "online": False,
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        connected = await client.check_connection()
        
        if not connected:
            return {
                "connected": False,
                "online": False,
                "error": "Unable to connect to Minecraft plugin API"
            }
        
        response = await client.get_status()
        
        if "error" in response:
            return {
                "connected": False,
                "online": False,
                "error": response["error"]
            }
        
        return {
            "connected": True,
            "server_version": response.get("serverVersion", "Unknown"),
            "minecraft_version": response.get("minecraftVersion", "Unknown"),
            "online": response.get("online", True),
        }
        
    except Exception as e:
        logger.error(f"Error in status_tool: {e}")
        return {
            "connected": False,
            "online": False,
            "error": f"Unexpected error: {str(e)}"
        }



async def plugins_tool(api_key: str) -> Dict[str, Any]:
    """Get list of installed Minecraft plugins."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "plugins": [],
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        response = await client.get_plugins()
        
        if "error" in response:
            return {
                "plugins": [],
                "error": response["error"]
            }
        
        plugins = response.get("plugins", [])
        formatted_plugins = []
        for plugin in plugins:
            formatted_plugins.append({
                "name": plugin.get("name", "Unknown"),
                "version": plugin.get("version", "Unknown"),
                "enabled": plugin.get("enabled", False),
                "authors": plugin.get("authors", [])
            })
        
        return {
            "plugins": formatted_plugins
        }
        
    except Exception as e:
        logger.error(f"Error in plugins_tool: {e}")
        return {
            "plugins": [],
            "error": f"Unexpected error: {str(e)}"
        }



async def send_command_tool(
    api_key: str,
    commands: Union[str, List[str]]
) -> Dict[str, Any]:
    """Send one or more commands to the Minecraft server."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "results": [],
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        if isinstance(commands, str):
            command_list = [commands]
        else:
            command_list = commands
        
        for cmd in command_list:
            if not cmd or not cmd.strip():
                return {
                    "results": [],
                    "error": "Command cannot be empty or contain only whitespace"
                }
        
        results = []
        
        for command in command_list:
            response = await client.execute_command(command.strip())
            
            if "error" in response:
                results.append({
                    "command": command,
                    "output": "",
                    "success": False,
                    "error": response["error"]
                })
            else:
                api_results = response.get("results", [])
                if api_results:
                    result = api_results[0]
                    results.append({
                        "command": result.get("command", command),
                        "output": result.get("output", ""),
                        "success": result.get("success", True),
                        "error": result.get("error")
                    })
                else:
                    results.append({
                        "command": command,
                        "output": "",
                        "success": True,
                        "error": None
                    })
        
        return {
            "results": results
        }
        
    except Exception as e:
        logger.error(f"Error in send_command_tool: {e}")
        return {
            "results": [],
            "error": f"Unexpected error: {str(e)}"
        }



async def get_logs_tool(
    api_key: str,
    limit: Optional[int] = None,
    start_time: Optional[str] = None,
    end_time: Optional[str] = None
) -> Dict[str, Any]:
    """Get server logs with optional filtering."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "logs": [],
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        start_dt = None
        end_dt = None
        
        if start_time:
            try:
                start_dt = datetime.fromisoformat(start_time.replace('Z', '+00:00'))
            except ValueError as e:
                return {
                    "logs": [],
                    "error": f"Invalid start_time format: {str(e)}"
                }
        
        if end_time:
            try:
                end_dt = datetime.fromisoformat(end_time.replace('Z', '+00:00'))
            except ValueError as e:
                return {
                    "logs": [],
                    "error": f"Invalid end_time format: {str(e)}"
                }
        
        response = await client.get_logs(
            limit=limit,
            start_time=start_dt,
            end_time=end_dt
        )
        
        if "error" in response:
            return {
                "logs": [],
                "error": response["error"]
            }
        
        logs = response.get("logs", [])
        formatted_logs = []
        
        for log in logs:
            formatted_logs.append({
                "timestamp": log.get("timestamp", ""),
                "level": log.get("level", "INFO"),
                "message": log.get("message", ""),
                "logger": log.get("logger", "")
            })
        
        return {
            "logs": formatted_logs
        }
        
    except Exception as e:
        logger.error(f"Error in get_logs_tool: {e}")
        return {
            "logs": [],
            "error": f"Unexpected error: {str(e)}"
        }



async def player_list_tool(api_key: str) -> Dict[str, Any]:
    """Get list of online players."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "count": 0,
            "players": [],
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        response = await client.get_players()
        
        if "error" in response:
            return {
                "count": 0,
                "players": [],
                "error": response["error"]
            }
        
        players = response.get("players", [])
        formatted_players = []
        for player in players:
            formatted_players.append({
                "name": player.get("name", "Unknown"),
                "uuid": player.get("uuid", ""),
                "ping": player.get("ping", 0)
            })
        
        return {
            "count": len(formatted_players),
            "players": formatted_players
        }
        
    except Exception as e:
        logger.error(f"Error in player_list_tool: {e}")
        return {
            "count": 0,
            "players": [],
            "error": f"Unexpected error: {str(e)}"
        }



async def get_errors_tool(
    api_key: str,
    plugin: Optional[str] = None,
    limit: Optional[int] = None
) -> Dict[str, Any]:
    """Get error logs with optional filtering."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "errors": [],
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        response = await client.get_filtered_logs(
            level="errors",
            plugin=plugin,
            limit=limit
        )
        
        if "error" in response:
            return {
                "errors": [],
                "error": response["error"]
            }
        
        errors = response.get("errors", [])
        formatted_errors = []
        
        for error in errors:
            formatted_errors.append({
                "timestamp": error.get("timestamp", ""),
                "plugin": error.get("plugin"),
                "message": error.get("message", ""),
                "stacktrace": error.get("stacktrace")
            })
        
        return {
            "errors": formatted_errors
        }
        
    except Exception as e:
        logger.error(f"Error in get_errors_tool: {e}")
        return {
            "errors": [],
            "error": f"Unexpected error: {str(e)}"
        }



async def get_warnings_tool(
    api_key: str,
    plugin: Optional[str] = None,
    limit: Optional[int] = None
) -> Dict[str, Any]:
    """Get warning logs with optional filtering."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "warnings": [],
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        response = await client.get_filtered_logs(
            level="warnings",
            plugin=plugin,
            limit=limit
        )
        
        if "error" in response:
            return {
                "warnings": [],
                "error": response["error"]
            }
        
        warnings = response.get("warnings", [])
        formatted_warnings = []
        
        for warning in warnings:
            formatted_warnings.append({
                "timestamp": warning.get("timestamp", ""),
                "plugin": warning.get("plugin"),
                "message": warning.get("message", "")
            })
        
        return {
            "warnings": formatted_warnings
        }
        
    except Exception as e:
        logger.error(f"Error in get_warnings_tool: {e}")
        return {
            "warnings": [],
            "error": f"Unexpected error: {str(e)}"
        }



async def get_commands_tool(api_key: str) -> Dict[str, Any]:
    """Get list of all registered commands on the server."""
    manager = get_connection_manager()
    client = manager.get_client(api_key)
    
    if client is None:
        return {
            "commands": [],
            "count": 0,
            "error": f"Not connected with this API key. Use 'connect' tool first."
        }
    try:
        response = await client.get_commands()
        
        if "error" in response:
            return {
                "commands": [],
                "count": 0,
                "error": response["error"]
            }
        
        commands = response.get("commands", [])
        formatted_commands = []
        for cmd in commands:
            formatted_commands.append({
                "name": cmd.get("name", "Unknown"),
                "description": cmd.get("description", ""),
                "usage": cmd.get("usage", ""),
                "aliases": cmd.get("aliases", []),
                "permission": cmd.get("permission", ""),
                "plugin": cmd.get("plugin", "Unknown")
            })
        
        return {
            "commands": formatted_commands,
            "count": len(formatted_commands)
        }
        
    except Exception as e:
        logger.error(f"Error in get_commands_tool: {e}")
        return {
            "commands": [],
            "count": 0,
            "error": f"Unexpected error: {str(e)}"
        }



async def connect_tool(
    url: str,
    api_key: str,
    timeout: int = 10
) -> Dict[str, Any]:
    """Connect to a Minecraft server."""
    try:
        manager = get_connection_manager()
        return await manager.connect(url, api_key, timeout)
        
    except Exception as e:
        logger.error(f"Error in connect_tool: {e}")
        return {
            "success": False,
            "error": f"Unexpected error: {str(e)}"
        }


async def disconnect_tool(api_key: str) -> Dict[str, Any]:
    """Disconnect from a Minecraft server by API key."""
    try:
        manager = get_connection_manager()
        return await manager.disconnect(api_key)
        
    except Exception as e:
        logger.error(f"Error in disconnect_tool: {e}")
        return {
            "success": False,
            "error": f"Unexpected error: {str(e)}"
        }


async def list_servers_tool() -> Dict[str, Any]:
    """List all connected servers."""
    try:
        manager = get_connection_manager()
        return manager.list_servers()
        
    except Exception as e:
        logger.error(f"Error in list_servers_tool: {e}")
        return {
            "servers": [],
            "count": 0,
            "error": f"Unexpected error: {str(e)}"
        }
