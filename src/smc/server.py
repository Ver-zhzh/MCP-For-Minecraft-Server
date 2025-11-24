"""Main MCP server implementation."""

import logging
import sys
import os
from typing import Any, Optional

from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import Tool, TextContent

from .config import ServerConfig
from .client import PluginAPIClient
from .tools import (
    status_tool,
    plugins_tool,
    send_command_tool,
    get_logs_tool,
    player_list_tool,
    get_errors_tool,
    get_warnings_tool,
    get_commands_tool,
    connect_tool,
    disconnect_tool,
    list_servers_tool,
    get_connection_manager,
)

logger = logging.getLogger(__name__)


def setup_logging():
    """Configure Python logging for the MCP server."""
    log_level_str = os.getenv('MINECRAFT_LOG_LEVEL', 'ERROR').upper()
    
    log_level_map = {
        'DEBUG': logging.DEBUG,
        'INFO': logging.INFO,
        'WARNING': logging.WARNING,
        'ERROR': logging.ERROR,
        'CRITICAL': logging.CRITICAL
    }
    
    log_level = log_level_map.get(log_level_str, logging.ERROR)
    log_file = os.getenv('MINECRAFT_LOG_FILE', 'smc-server.log')
    handlers = []
    
    try:
        file_handler = logging.FileHandler(log_file, mode='a', encoding='utf-8')
        file_handler.setFormatter(
            logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        )
        handlers.append(file_handler)
    except Exception:
        handlers.append(logging.NullHandler())
    
    logging.basicConfig(
        level=log_level,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        handlers=handlers
    )
    
    logging.getLogger('httpx').setLevel(logging.WARNING)
    logging.getLogger('httpcore').setLevel(logging.WARNING)
    logging.getLogger('mcp').setLevel(logging.WARNING)
    
    logger.info(f"Logging configured with level: {log_level_str}")
    logger.info(f"Log file: {log_file}")


def handle_exception(exc_type, exc_value, exc_traceback):
    """Global exception handler for uncaught exceptions."""
    if issubclass(exc_type, KeyboardInterrupt):
        sys.__excepthook__(exc_type, exc_value, exc_traceback)
        return
    
    logger.critical(
        "Uncaught exception",
        exc_info=(exc_type, exc_value, exc_traceback)
    )


async def main():
    """Main entry point for the MCP server."""
    setup_logging()
    sys.excepthook = handle_exception
    
    logger.info("=" * 60)
    logger.info("Minecraft Server MCP - Starting")
    logger.info("=" * 60)
    
    try:
        manager = get_connection_manager()
        logger.info("Connection manager initialized")
        logger.info("Use 'connect' tool to connect to Minecraft servers")
        
        server = Server("minecraft-server-mcp")
        logger.info("MCP server instance created")
        
        @server.list_tools()
        async def list_tools() -> list[Tool]:
            """List all available tools."""
            return [
                Tool(
                    name="connect",
                    description="Connect to a Minecraft server using API key as identifier",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "url": {
                                "type": "string",
                                "description": "Server API URL (e.g., http://localhost:8080)"
                            },
                            "api_key": {
                                "type": "string",
                                "description": "API key for authentication (also used as unique identifier)"
                            },
                            "timeout": {
                                "type": "integer",
                                "description": "Request timeout in seconds (default: 10)",
                                "default": 10
                            }
                        },
                        "required": ["url", "api_key"]
                    }
                ),
                Tool(
                    name="disconnect",
                    description="Disconnect from a Minecraft server by API key",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to disconnect"
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="list_servers",
                    description="List all connected Minecraft servers",
                    inputSchema={
                        "type": "object",
                        "properties": {},
                        "required": []
                    }
                ),
                Tool(
                    name="status",
                    description="Get Minecraft server status and information",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="plugins",
                    description="Get list of installed Minecraft plugins",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="send_command",
                    description="Send commands to the Minecraft server",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to send commands to"
                            },
                            "commands": {
                                "type": ["string", "array"],
                                "items": {"type": "string"},
                                "description": "Single command string or array of command strings"
                            }
                        },
                        "required": ["api_key", "commands"]
                    }
                ),
                Tool(
                    name="get_logs",
                    description="Get server logs with optional filters",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            },
                            "limit": {
                                "type": "integer",
                                "description": "Maximum number of log entries",
                                "minimum": 1
                            },
                            "start_time": {
                                "type": "string",
                                "description": "Start time (ISO 8601 format)"
                            },
                            "end_time": {
                                "type": "string",
                                "description": "End time (ISO 8601 format)"
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="player_list",
                    description="Get list of online players",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="get_errors",
                    description="Get error logs with optional filters",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            },
                            "plugin": {
                                "type": "string",
                                "description": "Filter by plugin name"
                            },
                            "limit": {
                                "type": "integer",
                                "description": "Maximum number of entries",
                                "minimum": 1
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="get_warnings",
                    description="Get warning logs with optional filters",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            },
                            "plugin": {
                                "type": "string",
                                "description": "Filter by plugin name"
                            },
                            "limit": {
                                "type": "integer",
                                "description": "Maximum number of entries",
                                "minimum": 1
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
                Tool(
                    name="get_commands",
                    description="Get list of all registered server commands",
                    inputSchema={
                        "type": "object",
                        "properties": {
                            "api_key": {
                                "type": "string",
                                "description": "API key of the server to query"
                            }
                        },
                        "required": ["api_key"]
                    }
                ),
            ]
        
        @server.call_tool()
        async def call_tool(name: str, arguments: Any) -> list[TextContent]:
            """Handle tool calls from the MCP client."""
            logger.info(f"Tool called: {name}")
            logger.debug(f"Tool {name} arguments: {arguments}")
            
            try:
                if not isinstance(arguments, dict):
                    logger.warning(f"Invalid arguments type for tool {name}: {type(arguments)}")
                    arguments = {}
                if name == "connect":
                    url = arguments.get("url")
                    api_key = arguments.get("api_key")
                    timeout = arguments.get("timeout", 10)
                    if not url or not api_key:
                        result = {"success": False, "error": "Missing required parameters: url and api_key"}
                    else:
                        logger.debug(f"Connecting to {url}")
                        result = await connect_tool(url, api_key, timeout)
                
                elif name == "disconnect":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"success": False, "error": "Missing required parameter: api_key"}
                    else:
                        logger.debug(f"Disconnecting server")
                        result = await disconnect_tool(api_key)
                
                elif name == "list_servers":
                    logger.debug("Listing all servers")
                    result = await list_servers_tool()
                
                elif name == "status":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"connected": False, "error": "Missing required parameter: api_key"}
                    else:
                        logger.debug("Getting server status")
                        result = await status_tool(api_key)
                    
                elif name == "plugins":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"plugins": [], "error": "Missing required parameter: api_key"}
                    else:
                        logger.debug("Getting plugins list")
                        result = await plugins_tool(api_key)
                    
                elif name == "send_command":
                    api_key = arguments.get("api_key")
                    commands = arguments.get("commands")
                    if not api_key:
                        result = {"results": [], "error": "Missing required parameter: api_key"}
                    elif not commands:
                        result = {"results": [], "error": "Missing required parameter: commands"}
                    else:
                        logger.debug(f"Sending command(s)")
                        result = await send_command_tool(api_key, commands)
                    
                elif name == "get_logs":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"logs": [], "error": "Missing required parameter: api_key"}
                    else:
                        limit = arguments.get("limit")
                        start_time = arguments.get("start_time")
                        end_time = arguments.get("end_time")
                        logger.debug(f"Getting logs (limit={limit})")
                        result = await get_logs_tool(api_key, limit, start_time, end_time)
                    
                elif name == "player_list":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"count": 0, "players": [], "error": "Missing required parameter: api_key"}
                    else:
                        logger.debug("Getting player list")
                        result = await player_list_tool(api_key)
                    
                elif name == "get_errors":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"errors": [], "error": "Missing required parameter: api_key"}
                    else:
                        plugin = arguments.get("plugin")
                        limit = arguments.get("limit")
                        logger.debug(f"Getting errors (plugin={plugin}, limit={limit})")
                        result = await get_errors_tool(api_key, plugin, limit)
                    
                elif name == "get_warnings":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"warnings": [], "error": "Missing required parameter: api_key"}
                    else:
                        plugin = arguments.get("plugin")
                        limit = arguments.get("limit")
                        logger.debug(f"Getting warnings (plugin={plugin}, limit={limit})")
                        result = await get_warnings_tool(api_key, plugin, limit)
                    
                elif name == "get_commands":
                    api_key = arguments.get("api_key")
                    if not api_key:
                        result = {"commands": [], "count": 0, "error": "Missing required parameter: api_key"}
                    else:
                        logger.debug("Getting commands list")
                        result = await get_commands_tool(api_key)
                    
                else:
                    logger.error(f"Unknown tool requested: {name}")
                    result = {"error": f"Unknown tool: {name}"}
                
                if "error" in result:
                    logger.warning(f"Tool {name} returned error: {result['error']}")
                else:
                    logger.info(f"Tool {name} completed successfully")
                    logger.debug(f"Tool {name} result: {result}")
                
                import json
                return [TextContent(
                    type="text",
                    text=json.dumps(result, indent=2, ensure_ascii=False)
                )]
                
            except KeyboardInterrupt:
                raise
                
            except Exception as e:
                logger.error(
                    f"Unexpected error executing tool {name}: {type(e).__name__}: {e}",
                    exc_info=True
                )
                
                import json
                error_result = {
                    "error": f"Tool execution failed: {str(e)}",
                    "error_type": type(e).__name__
                }
                
                return [TextContent(
                    type="text",
                    text=json.dumps(error_result, indent=2)
                )]
        
        logger.info("Starting MCP server with stdio transport")
        async with stdio_server() as (read_stream, write_stream):
            await server.run(
                read_stream,
                write_stream,
                server.create_initialization_options()
            )
        
    except ValueError as e:
        logger.error("=" * 60)
        logger.error("CONFIGURATION ERROR")
        logger.error("=" * 60)
        logger.error(f"Error: {e}")
        logger.error("")
        logger.error("Please ensure the following environment variables are set:")
        logger.error("  - MINECRAFT_PLUGIN_URL: Base URL for the plugin API")
        logger.error("  - MINECRAFT_API_KEY: API key for authentication")
        logger.error("")
        logger.error("Optional environment variables:")
        logger.error("  - MINECRAFT_REQUEST_TIMEOUT: Request timeout in seconds (default: 10)")
        logger.error("  - MINECRAFT_LOG_LEVEL: Log level (DEBUG, INFO, WARNING, ERROR)")
        logger.error("=" * 60)
        raise
    
    except KeyboardInterrupt:
        logger.info("MCP server stopped by user (KeyboardInterrupt)")
        raise
    
    except Exception as e:
        logger.error("=" * 60)
        logger.error("UNEXPECTED ERROR")
        logger.error("=" * 60)
        logger.error(f"Error type: {type(e).__name__}")
        logger.error(f"Error message: {e}")
        logger.error("", exc_info=True)
        logger.error("=" * 60)
        raise
    
    finally:
        logger.info("Cleaning up resources")
        try:
            manager = get_connection_manager()
            await manager.close_all()
            logger.info("All connections closed successfully")
        except Exception as e:
            logger.warning(f"Error closing connections: {e}")
        
        logger.info("=" * 60)
        logger.info("Minecraft Server MCP - Stopped")
        logger.info("=" * 60)
