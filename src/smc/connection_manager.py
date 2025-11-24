"""Connection manager for multiple Minecraft servers."""

import logging
from typing import Dict, Optional, List
from .client import PluginAPIClient

logger = logging.getLogger(__name__)


class ConnectionManager:
    """Manages connections to multiple Minecraft servers using API key as identifier."""
    
    def __init__(self):
        self.connections: Dict[str, PluginAPIClient] = {}
        self.server_info: Dict[str, Dict] = {}
        
    async def connect(
        self,
        url: str,
        api_key: str,
        timeout: int = 10
    ) -> Dict[str, any]:
        if api_key in self.connections:
            return {
                "success": False,
                "error": f"Already connected with this API key",
                "api_key": api_key[:8] + "..."
            }
        
        try:
            client = PluginAPIClient(
                base_url=url,
                api_key=api_key,
                timeout=timeout
            )
            
            connected = await client.check_connection()
            if not connected:
                return {
                    "success": False,
                    "error": "Failed to connect to server - connection test failed",
                    "url": url
                }
            
            status = await client.get_status()
            if "error" in status:
                return {
                    "success": False,
                    "error": f"Connected but failed to get status: {status['error']}",
                    "url": url
                }
            
            self.connections[api_key] = client
            self.server_info[api_key] = {
                "url": url,
                "connected_at": __import__('datetime').datetime.now().isoformat()
            }
                
            logger.info(f"Connected to server at {url}")
            
            return {
                "success": True,
                "api_key": api_key[:8] + "...",
                "url": url,
                "server_info": {
                    "server_version": status.get("serverVersion", "Unknown"),
                    "minecraft_version": status.get("minecraftVersion", "Unknown"),
                    "online": status.get("online", False)
                }
            }
            
        except Exception as e:
            logger.error(f"Failed to connect to {url}: {e}")
            return {
                "success": False,
                "error": str(e),
                "url": url
            }
    
    async def disconnect(self, api_key: str) -> Dict[str, any]:
        if api_key not in self.connections:
            return {
                "success": False,
                "error": f"Not connected with this API key"
            }
        
        try:
            info = self.server_info.get(api_key, {})
            url = info.get("url", "Unknown")
            
            client = self.connections.pop(api_key)
            self.server_info.pop(api_key, None)
            
            await client.close()
                    
            logger.info(f"Disconnected from server at {url}")
            
            return {
                "success": True,
                "api_key": api_key[:8] + "...",
                "url": url,
                "remaining_connections": len(self.connections)
            }
            
        except Exception as e:
            logger.error(f"Failed to disconnect: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    def list_servers(self) -> Dict[str, any]:
        servers = []
        for api_key, client in self.connections.items():
            info = self.server_info.get(api_key, {})
            servers.append({
                "api_key": api_key[:8] + "...",
                "url": client.base_url,
                "connected_at": info.get("connected_at", "Unknown")
            })
        
        return {
            "servers": servers,
            "count": len(servers)
        }
    
    def get_client(self, api_key: str) -> Optional[PluginAPIClient]:
        return self.connections.get(api_key)
    
    def has_connections(self) -> bool:
        return len(self.connections) > 0
    
    async def close_all(self):
        for api_key, client in list(self.connections.items()):
            try:
                await client.close()
                info = self.server_info.get(api_key, {})
                url = info.get("url", "Unknown")
                logger.info(f"Closed connection to {url}")
            except Exception as e:
                logger.warning(f"Error closing connection: {e}")
        
        self.connections.clear()
        self.server_info.clear()
