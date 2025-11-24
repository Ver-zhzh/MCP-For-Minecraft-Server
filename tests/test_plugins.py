"""Tests for the plugins tool."""

import pytest
from unittest.mock import AsyncMock, MagicMock
from smc.tools import plugins_tool
from smc.client import PluginAPIClient


@pytest.mark.asyncio
async def test_plugins_tool_success():
    """Test plugins tool returns plugin list successfully (Requirement 2.1)."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_plugins = AsyncMock(return_value={
        "plugins": [
            {
                "name": "TestPlugin",
                "version": "1.0.0",
                "enabled": True,
                "authors": ["TestAuthor"]
            },
            {
                "name": "AnotherPlugin",
                "version": "2.1.0",
                "enabled": False,
                "authors": ["Author1", "Author2"]
            }
        ]
    })
    
    # Call the tool
    result = await plugins_tool(client)
    
    # Verify the call
    client.get_plugins.assert_called_once()
    
    # Verify the result
    assert "plugins" in result
    assert len(result["plugins"]) == 2
    assert result["plugins"][0]["name"] == "TestPlugin"
    assert result["plugins"][0]["version"] == "1.0.0"
    assert result["plugins"][0]["enabled"] is True
    assert result["plugins"][0]["authors"] == ["TestAuthor"]
    assert result["plugins"][1]["name"] == "AnotherPlugin"


@pytest.mark.asyncio
async def test_plugins_tool_structure_integrity():
    """Test that all plugin entries have required fields (Requirement 2.2)."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_plugins = AsyncMock(return_value={
        "plugins": [
            {
                "name": "Plugin1",
                "version": "1.0",
                "enabled": True,
                "authors": ["Author1"]
            },
            {
                "name": "Plugin2",
                "version": "2.0",
                "enabled": False,
                "authors": []
            }
        ]
    })
    
    # Call the tool
    result = await plugins_tool(client)
    
    # Verify all entries have required fields (name, version, enabled)
    assert "plugins" in result
    for plugin in result["plugins"]:
        assert "name" in plugin
        assert "version" in plugin
        assert "enabled" in plugin
        assert isinstance(plugin["name"], str)
        assert isinstance(plugin["version"], str)
        assert isinstance(plugin["enabled"], bool)


@pytest.mark.asyncio
async def test_plugins_tool_empty_list():
    """Test plugins tool when no plugins are installed (Requirement 2.3)."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_plugins = AsyncMock(return_value={
        "plugins": []
    })
    
    # Call the tool
    result = await plugins_tool(client)
    
    # Verify the result is an empty list
    assert "plugins" in result
    assert len(result["plugins"]) == 0
    assert result["plugins"] == []


@pytest.mark.asyncio
async def test_plugins_tool_api_error():
    """Test plugins tool when API returns an error (Requirement 2.4)."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_plugins = AsyncMock(return_value={
        "error": "Connection failed to server"
    })
    
    # Call the tool
    result = await plugins_tool(client)
    
    # Verify the result contains error
    assert "error" in result
    assert result["error"] == "Connection failed to server"
    assert "plugins" in result
    assert len(result["plugins"]) == 0


@pytest.mark.asyncio
async def test_plugins_tool_missing_fields():
    """Test plugins tool handles missing fields gracefully."""
    # Create mock client with incomplete data
    client = MagicMock(spec=PluginAPIClient)
    client.get_plugins = AsyncMock(return_value={
        "plugins": [
            {
                "name": "IncompletePlugin"
                # Missing version, enabled, authors
            },
            {
                "version": "1.0",
                "enabled": True
                # Missing name, authors
            }
        ]
    })
    
    # Call the tool
    result = await plugins_tool(client)
    
    # Verify the tool provides defaults for missing fields
    assert "plugins" in result
    assert len(result["plugins"]) == 2
    
    # First plugin should have defaults
    assert result["plugins"][0]["name"] == "IncompletePlugin"
    assert result["plugins"][0]["version"] == "Unknown"
    assert result["plugins"][0]["enabled"] is False
    assert result["plugins"][0]["authors"] == []
    
    # Second plugin should have defaults
    assert result["plugins"][1]["name"] == "Unknown"
    assert result["plugins"][1]["version"] == "1.0"
    assert result["plugins"][1]["enabled"] is True


@pytest.mark.asyncio
async def test_plugins_tool_exception_handling():
    """Test plugins tool handles unexpected exceptions."""
    # Create mock client that raises an exception
    client = MagicMock(spec=PluginAPIClient)
    client.get_plugins = AsyncMock(side_effect=Exception("Unexpected error"))
    
    # Call the tool
    result = await plugins_tool(client)
    
    # Verify the result contains error
    assert "error" in result
    assert "Unexpected error" in result["error"]
    assert "plugins" in result
    assert len(result["plugins"]) == 0
