"""Configuration management for the MCP server."""

import os
from typing import Optional
from pydantic import BaseModel, Field, field_validator


class ServerConfig(BaseModel):
    """Configuration for the MCP server."""
    
    plugin_api_url: str = Field(
        description="Base URL for the Minecraft plugin API (e.g., http://localhost:8080)"
    )
    api_key: str = Field(
        description="API key for authenticating with the plugin"
    )
    request_timeout: int = Field(
        default=10,
        description="Timeout in seconds for HTTP requests"
    )
    
    @field_validator('plugin_api_url')
    @classmethod
    def validate_url(cls, v: str) -> str:
        if not v:
            raise ValueError("plugin_api_url cannot be empty")
        
        v = v.strip()
        if not v.startswith(('http://', 'https://')):
            raise ValueError("plugin_api_url must start with http:// or https://")
        
        return v.rstrip('/')
    
    @field_validator('api_key')
    @classmethod
    def validate_api_key(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("api_key cannot be empty")
        return v.strip()
    
    @field_validator('request_timeout')
    @classmethod
    def validate_timeout(cls, v: int) -> int:
        if v <= 0:
            raise ValueError("request_timeout must be positive")
        return v
    
    @classmethod
    def from_env(cls) -> "ServerConfig":
        plugin_api_url = os.getenv('MINECRAFT_PLUGIN_URL')
        api_key = os.getenv('MINECRAFT_API_KEY')
        request_timeout_str = os.getenv('MINECRAFT_REQUEST_TIMEOUT', '10')
        
        if not plugin_api_url:
            raise ValueError(
                "Missing required environment variable: MINECRAFT_PLUGIN_URL"
            )
        
        if not api_key:
            raise ValueError(
                "Missing required environment variable: MINECRAFT_API_KEY"
            )
        
        try:
            request_timeout = int(request_timeout_str)
        except ValueError:
            raise ValueError(
                f"Invalid MINECRAFT_REQUEST_TIMEOUT value: {request_timeout_str}. "
                "Must be an integer."
            )
        
        return cls(
            plugin_api_url=plugin_api_url,
            api_key=api_key,
            request_timeout=request_timeout
        )
