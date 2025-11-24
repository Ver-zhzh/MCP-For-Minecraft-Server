"""Tests for the get_warnings tool."""

import pytest
from unittest.mock import AsyncMock, MagicMock
from smc.tools import get_warnings_tool
from smc.client import PluginAPIClient


@pytest.mark.asyncio
async def test_get_warnings_no_filter():
    """Test get_warnings tool without filters."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "warnings": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "TestPlugin",
                "message": "Test warning message"
            }
        ]
    })
    
    # Call the tool
    result = await get_warnings_tool(client)
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="warnings",
        plugin=None,
        limit=None
    )
    
    # Verify the result
    assert "warnings" in result
    assert len(result["warnings"]) == 1
    assert result["warnings"][0]["timestamp"] == "2024-01-01T12:00:00Z"
    assert result["warnings"][0]["plugin"] == "TestPlugin"
    assert result["warnings"][0]["message"] == "Test warning message"


@pytest.mark.asyncio
async def test_get_warnings_with_plugin_filter():
    """Test get_warnings tool with plugin filter."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "warnings": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "MyPlugin",
                "message": "Plugin warning"
            }
        ]
    })
    
    # Call the tool with plugin filter
    result = await get_warnings_tool(client, plugin="MyPlugin")
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="warnings",
        plugin="MyPlugin",
        limit=None
    )
    
    # Verify the result
    assert "warnings" in result
    assert len(result["warnings"]) == 1
    assert result["warnings"][0]["plugin"] == "MyPlugin"


@pytest.mark.asyncio
async def test_get_warnings_with_limit():
    """Test get_warnings tool with limit parameter."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "warnings": [
            {"timestamp": "2024-01-01T12:00:00Z", "plugin": "P1", "message": "W1"},
            {"timestamp": "2024-01-01T12:01:00Z", "plugin": "P2", "message": "W2"}
        ]
    })
    
    # Call the tool with limit
    result = await get_warnings_tool(client, limit=2)
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="warnings",
        plugin=None,
        limit=2
    )
    
    # Verify the result
    assert "warnings" in result
    assert len(result["warnings"]) == 2


@pytest.mark.asyncio
async def test_get_warnings_empty_result():
    """Test get_warnings tool when no warnings are found."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "warnings": []
    })
    
    # Call the tool
    result = await get_warnings_tool(client)
    
    # Verify the result
    assert "warnings" in result
    assert len(result["warnings"]) == 0


@pytest.mark.asyncio
async def test_get_warnings_api_error():
    """Test get_warnings tool when API returns an error."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "error": "Connection failed"
    })
    
    # Call the tool
    result = await get_warnings_tool(client)
    
    # Verify the result contains error
    assert "error" in result
    assert result["error"] == "Connection failed"
    assert "warnings" in result
    assert len(result["warnings"]) == 0


@pytest.mark.asyncio
async def test_get_warnings_structure_integrity():
    """Test that all warning entries have required fields (Requirement 7.5)."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "warnings": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "TestPlugin",
                "message": "Warning message"
            },
            {
                "timestamp": "2024-01-01T12:01:00Z",
                "plugin": None,
                "message": "Another warning"
            }
        ]
    })
    
    # Call the tool
    result = await get_warnings_tool(client)
    
    # Verify all entries have required fields
    assert "warnings" in result
    for warning in result["warnings"]:
        assert "timestamp" in warning
        assert "message" in warning
        # plugin can be None but must be present
        assert "plugin" in warning


@pytest.mark.asyncio
async def test_get_warnings_with_both_filters():
    """Test get_warnings tool with both plugin and limit filters."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "warnings": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "SpecificPlugin",
                "message": "Filtered warning"
            }
        ]
    })
    
    # Call the tool with both filters
    result = await get_warnings_tool(client, plugin="SpecificPlugin", limit=10)
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="warnings",
        plugin="SpecificPlugin",
        limit=10
    )
    
    # Verify the result
    assert "warnings" in result
    assert len(result["warnings"]) == 1
    assert result["warnings"][0]["plugin"] == "SpecificPlugin"
