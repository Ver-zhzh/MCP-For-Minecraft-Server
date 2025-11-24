"""HTTP client for communicating with the Minecraft plugin."""

import httpx
from typing import Optional, Dict, Any, List
from datetime import datetime
import logging

logger = logging.getLogger(__name__)


class PluginAPIClient:
    """HTTP client for communicating with the Minecraft plugin API."""
    
    def __init__(self, base_url: str, api_key: str, timeout: int = 10):
        self.base_url = base_url.rstrip('/')
        self.api_key = api_key
        self.timeout = timeout
        self._client: Optional[httpx.AsyncClient] = None
    
    async def __aenter__(self):
        self._client = httpx.AsyncClient(
            timeout=self.timeout,
            headers={"X-API-Key": self.api_key}
        )
        return self
    
    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if self._client:
            await self._client.aclose()
            self._client = None
    
    def _get_client(self) -> httpx.AsyncClient:
        if self._client is None:
            self._client = httpx.AsyncClient(
                timeout=self.timeout,
                headers={"X-API-Key": self.api_key}
            )
        return self._client
    
    async def close(self):
        if self._client:
            await self._client.aclose()
            self._client = None
    
    async def check_connection(self) -> bool:
        try:
            client = self._get_client()
            response = await client.get(f"{self.base_url}/api/status")
            return response.status_code == 200
        except httpx.HTTPError as e:
            logger.error(f"Connection check failed: {e}")
            return False
        except Exception as e:
            logger.error(f"Unexpected error during connection check: {e}")
            return False
    
    async def _request(
        self,
        method: str,
        endpoint: str,
        max_retries: int = 2,
        **kwargs
    ) -> Dict[str, Any]:
        client = self._get_client()
        url = f"{self.base_url}{endpoint}"
        
        last_error = None
        for attempt in range(max_retries + 1):
            try:
                response = await client.request(method, url, **kwargs)
                
                if response.status_code == 401:
                    return {
                        "error": "Authentication failed. Invalid API key.",
                        "status_code": 401
                    }
                
                if response.status_code >= 400:
                    error_msg = f"HTTP {response.status_code}"
                    try:
                        error_data = response.json()
                        if "error" in error_data:
                            error_msg = error_data["error"]
                    except Exception:
                        error_msg = response.text or error_msg
                    
                    return {
                        "error": error_msg,
                        "status_code": response.status_code
                    }
                
                try:
                    return response.json()
                except Exception:
                    return {"data": response.text}
                    
            except httpx.TimeoutException as e:
                last_error = f"Request timeout after {self.timeout} seconds"
                logger.warning(f"Attempt {attempt + 1}/{max_retries + 1} failed: {last_error}")
                
            except httpx.ConnectError as e:
                last_error = f"Connection failed: {str(e)}"
                logger.warning(f"Attempt {attempt + 1}/{max_retries + 1} failed: {last_error}")
                
            except httpx.HTTPError as e:
                last_error = f"HTTP error: {str(e)}"
                logger.warning(f"Attempt {attempt + 1}/{max_retries + 1} failed: {last_error}")
                
            except Exception as e:
                last_error = f"Unexpected error: {str(e)}"
                logger.error(f"Attempt {attempt + 1}/{max_retries + 1} failed: {last_error}")
        
        return {
            "error": last_error or "Request failed after all retries",
            "status_code": 0
        }
    
    async def get_status(self) -> Dict[str, Any]:
        return await self._request("GET", "/api/status")
    
    async def get_plugins(self) -> Dict[str, Any]:
        return await self._request("GET", "/api/plugins")
    
    async def execute_command(self, command: str) -> Dict[str, Any]:
        return await self._request(
            "POST",
            "/api/command",
            json={"commands": command}
        )
    
    async def get_logs(
        self,
        limit: Optional[int] = None,
        start_time: Optional[datetime] = None,
        end_time: Optional[datetime] = None
    ) -> Dict[str, Any]:
        params = {}
        if limit is not None:
            params["limit"] = limit
        if start_time is not None:
            params["start_time"] = start_time.isoformat()
        if end_time is not None:
            params["end_time"] = end_time.isoformat()
        
        return await self._request("GET", "/api/logs", params=params)
    
    async def get_players(self) -> Dict[str, Any]:
        return await self._request("GET", "/api/players")
    
    async def get_filtered_logs(
        self,
        level: str,
        plugin: Optional[str] = None,
        limit: Optional[int] = None
    ) -> Dict[str, Any]:
        endpoint = f"/api/logs/{level}"
        params = {}
        if plugin is not None:
            params["plugin"] = plugin
        if limit is not None:
            params["limit"] = limit
        
        return await self._request("GET", endpoint, params=params)

    async def get_commands(self) -> Dict[str, Any]:
        return await self._request("GET", "/api/commands")
