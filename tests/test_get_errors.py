"""Tests for the get_errors tool."""

import pytest
from unittest.mock import AsyncMock, MagicMock
from smc.tools import get_errors_tool
from smc.client import PluginAPIClient


@pytest.mark.asyncio
async def test_get_errors_no_filter():
    """Test get_errors tool without filters."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "errors": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "TestPlugin",
                "message": "Test error message",
                "stacktrace": "Stack trace here"
            }
        ]
    })
    
    # Call the tool
    result = await get_errors_tool(client)
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="errors",
        plugin=None,
        limit=None
    )
    
    # Verify the result
    assert "errors" in result
    assert len(result["errors"]) == 1
    assert result["errors"][0]["timestamp"] == "2024-01-01T12:00:00Z"
    assert result["errors"][0]["plugin"] == "TestPlugin"
    assert result["errors"][0]["message"] == "Test error message"
    assert result["errors"][0]["stacktrace"] == "Stack trace here"


@pytest.mark.asyncio
async def test_get_errors_with_plugin_filter():
    """Test get_errors tool with plugin filter."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "errors": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "MyPlugin",
                "message": "Plugin error",
                "stacktrace": None
            }
        ]
    })
    
    # Call the tool with plugin filter
    result = await get_errors_tool(client, plugin="MyPlugin")
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="errors",
        plugin="MyPlugin",
        limit=None
    )
    
    # Verify the result
    assert "errors" in result
    assert len(result["errors"]) == 1
    assert result["errors"][0]["plugin"] == "MyPlugin"


@pytest.mark.asyncio
async def test_get_errors_with_limit():
    """Test get_errors tool with limit parameter."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "errors": [
            {"timestamp": "2024-01-01T12:00:00Z", "plugin": "P1", "message": "E1", "stacktrace": None},
            {"timestamp": "2024-01-01T12:01:00Z", "plugin": "P2", "message": "E2", "stacktrace": None}
        ]
    })
    
    # Call the tool with limit
    result = await get_errors_tool(client, limit=2)
    
    # Verify the call
    client.get_filtered_logs.assert_called_once_with(
        level="errors",
        plugin=None,
        limit=2
    )
    
    # Verify the result
    assert "errors" in result
    assert len(result["errors"]) == 2


@pytest.mark.asyncio
async def test_get_errors_empty_result():
    """Test get_errors tool when no errors are found."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "errors": []
    })
    
    # Call the tool
    result = await get_errors_tool(client)
    
    # Verify the result
    assert "errors" in result
    assert len(result["errors"]) == 0


@pytest.mark.asyncio
async def test_get_errors_api_error():
    """Test get_errors tool when API returns an error."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "error": "Connection failed"
    })
    
    # Call the tool
    result = await get_errors_tool(client)
    
    # Verify the result contains error
    assert "error" in result
    assert result["error"] == "Connection failed"
    assert "errors" in result
    assert len(result["errors"]) == 0


@pytest.mark.asyncio
async def test_get_errors_structure_integrity():
    """Test that all error entries have required fields (Requirement 6.5)."""
    # Create mock client
    client = MagicMock(spec=PluginAPIClient)
    client.get_filtered_logs = AsyncMock(return_value={
        "errors": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "plugin": "TestPlugin",
                "message": "Error message",
                "stacktrace": "Stack trace"
            },
            {
                "timestamp": "2024-01-01T12:01:00Z",
                "plugin": None,
                "message": "Another error",
                "stacktrace": None
            }
        ]
    })
    
    # Call the tool
    result = await get_errors_tool(client)
    
    # Verify all entries have required fields
    assert "errors" in result
    for error in result["errors"]:
        assert "timestamp" in error
        assert "message" in error
        # plugin and stacktrace can be None but must be present
        assert "plugin" in error
        assert "stacktrace" in error
