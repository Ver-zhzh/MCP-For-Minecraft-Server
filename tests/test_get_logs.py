"""Tests for get_logs tool.

Requirements: 4.1, 4.2, 4.3, 4.5
"""

import pytest
from datetime import datetime, timezone
from unittest.mock import AsyncMock, MagicMock
from smc.tools import get_logs_tool
from smc.client import PluginAPIClient


@pytest.fixture
def mock_client():
    """Create a mock API client."""
    client = MagicMock(spec=PluginAPIClient)
    return client


@pytest.mark.asyncio
async def test_get_logs_no_filters(mock_client):
    """Test getting logs without any filters.
    
    Requirement 4.1: Get all available logs
    """
    # Mock response
    mock_client.get_logs = AsyncMock(return_value={
        "logs": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "level": "INFO",
                "message": "Server started",
                "logger": "Minecraft"
            },
            {
                "timestamp": "2024-01-01T12:01:00Z",
                "level": "WARN",
                "message": "Low memory",
                "logger": "System"
            }
        ]
    })
    
    result = await get_logs_tool(mock_client)
    
    assert "logs" in result
    assert len(result["logs"]) == 2
    assert result["logs"][0]["timestamp"] == "2024-01-01T12:00:00Z"
    assert result["logs"][0]["level"] == "INFO"
    assert result["logs"][0]["message"] == "Server started"
    assert result["logs"][0]["logger"] == "Minecraft"
    
    # Verify client was called correctly
    mock_client.get_logs.assert_called_once_with(
        limit=None,
        start_time=None,
        end_time=None
    )


@pytest.mark.asyncio
async def test_get_logs_with_limit(mock_client):
    """Test getting logs with limit.
    
    Requirement 4.2: Return specified number of log entries
    """
    # Mock response with 3 logs
    mock_client.get_logs = AsyncMock(return_value={
        "logs": [
            {"timestamp": "2024-01-01T12:00:00Z", "level": "INFO", "message": "Log 1", "logger": "Test"},
            {"timestamp": "2024-01-01T12:01:00Z", "level": "INFO", "message": "Log 2", "logger": "Test"},
            {"timestamp": "2024-01-01T12:02:00Z", "level": "INFO", "message": "Log 3", "logger": "Test"}
        ]
    })
    
    result = await get_logs_tool(mock_client, limit=3)
    
    assert "logs" in result
    assert len(result["logs"]) == 3
    
    # Verify client was called with limit
    mock_client.get_logs.assert_called_once_with(
        limit=3,
        start_time=None,
        end_time=None
    )


@pytest.mark.asyncio
async def test_get_logs_with_time_range(mock_client):
    """Test getting logs with time range filter.
    
    Requirement 4.3: Return logs within specified time range
    """
    # Mock response
    mock_client.get_logs = AsyncMock(return_value={
        "logs": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "level": "INFO",
                "message": "Log in range",
                "logger": "Test"
            }
        ]
    })
    
    start_time = "2024-01-01T12:00:00Z"
    end_time = "2024-01-01T13:00:00Z"
    
    result = await get_logs_tool(mock_client, start_time=start_time, end_time=end_time)
    
    assert "logs" in result
    assert len(result["logs"]) == 1
    
    # Verify client was called with parsed datetime objects
    call_args = mock_client.get_logs.call_args
    assert call_args.kwargs["limit"] is None
    assert isinstance(call_args.kwargs["start_time"], datetime)
    assert isinstance(call_args.kwargs["end_time"], datetime)


@pytest.mark.asyncio
async def test_get_logs_structure_completeness(mock_client):
    """Test that log entries contain all required fields.
    
    Requirement 4.5: Each log entry must have timestamp, level, and message
    """
    # Mock response
    mock_client.get_logs = AsyncMock(return_value={
        "logs": [
            {
                "timestamp": "2024-01-01T12:00:00Z",
                "level": "ERROR",
                "message": "Test error",
                "logger": "TestPlugin"
            }
        ]
    })
    
    result = await get_logs_tool(mock_client)
    
    assert "logs" in result
    assert len(result["logs"]) == 1
    
    log_entry = result["logs"][0]
    # Verify all required fields are present
    assert "timestamp" in log_entry
    assert "level" in log_entry
    assert "message" in log_entry
    assert "logger" in log_entry
    
    assert log_entry["timestamp"] == "2024-01-01T12:00:00Z"
    assert log_entry["level"] == "ERROR"
    assert log_entry["message"] == "Test error"
    assert log_entry["logger"] == "TestPlugin"


@pytest.mark.asyncio
async def test_get_logs_error_handling(mock_client):
    """Test error handling when API returns error."""
    # Mock error response
    mock_client.get_logs = AsyncMock(return_value={
        "error": "Failed to read log file"
    })
    
    result = await get_logs_tool(mock_client)
    
    assert "logs" in result
    assert "error" in result
    assert result["logs"] == []
    assert result["error"] == "Failed to read log file"


@pytest.mark.asyncio
async def test_get_logs_invalid_time_format(mock_client):
    """Test handling of invalid time format."""
    result = await get_logs_tool(
        mock_client,
        start_time="invalid-time-format"
    )
    
    assert "logs" in result
    assert "error" in result
    assert result["logs"] == []
    assert "Invalid start_time format" in result["error"]


@pytest.mark.asyncio
async def test_get_logs_empty_response(mock_client):
    """Test handling of empty log response."""
    # Mock empty response
    mock_client.get_logs = AsyncMock(return_value={
        "logs": []
    })
    
    result = await get_logs_tool(mock_client)
    
    assert "logs" in result
    assert result["logs"] == []
    assert "error" not in result


@pytest.mark.asyncio
async def test_get_logs_exception_handling(mock_client):
    """Test handling of unexpected exceptions."""
    # Mock exception
    mock_client.get_logs = AsyncMock(side_effect=Exception("Unexpected error"))
    
    result = await get_logs_tool(mock_client)
    
    assert "logs" in result
    assert "error" in result
    assert result["logs"] == []
    assert "Unexpected error" in result["error"]
