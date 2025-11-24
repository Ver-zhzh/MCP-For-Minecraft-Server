"""Tests for the MCP server main entry point."""

import pytest
import os
from unittest.mock import patch, MagicMock, AsyncMock
from smc.server import setup_logging, handle_exception, main
from smc.config import ServerConfig


def test_setup_logging_default():
    """Test that logging setup works with default settings."""
    setup_logging()
    # If no exception is raised, the test passes


def test_setup_logging_with_debug():
    """Test that logging setup works with DEBUG level."""
    with patch.dict(os.environ, {'MINECRAFT_LOG_LEVEL': 'DEBUG'}):
        setup_logging()
        # If no exception is raised, the test passes


def test_setup_logging_with_invalid_level():
    """Test that logging setup handles invalid log level gracefully."""
    with patch.dict(os.environ, {'MINECRAFT_LOG_LEVEL': 'INVALID'}):
        setup_logging()
        # Should default to INFO level


def test_handle_exception():
    """Test that global exception handler logs exceptions."""
    import logging
    
    # Create a test exception
    try:
        raise ValueError("Test exception")
    except ValueError:
        import sys
        exc_info = sys.exc_info()
        
        # Call the handler (should not raise)
        handle_exception(exc_info[0], exc_info[1], exc_info[2])


def test_handle_exception_keyboard_interrupt():
    """Test that KeyboardInterrupt is handled specially."""
    import sys
    
    # Mock the default exception hook
    original_hook = sys.__excepthook__
    sys.__excepthook__ = MagicMock()
    
    try:
        # Create a KeyboardInterrupt
        try:
            raise KeyboardInterrupt()
        except KeyboardInterrupt:
            exc_info = sys.exc_info()
            
            # Call the handler
            handle_exception(exc_info[0], exc_info[1], exc_info[2])
            
            # Verify the default hook was called
            sys.__excepthook__.assert_called_once()
    finally:
        sys.__excepthook__ = original_hook


@pytest.mark.asyncio
async def test_main_missing_config():
    """Test that main() raises ValueError when configuration is missing."""
    # Clear environment variables
    with patch.dict(os.environ, {}, clear=True):
        with pytest.raises(ValueError, match="Missing required environment variable"):
            await main()


@pytest.mark.asyncio
async def test_main_with_valid_config():
    """Test that main() can be initialized with valid configuration."""
    # Set up environment variables
    env_vars = {
        'MINECRAFT_PLUGIN_URL': 'http://localhost:8080',
        'MINECRAFT_API_KEY': 'test-api-key',
        'MINECRAFT_LOG_LEVEL': 'ERROR'  # Reduce log noise
    }
    
    with patch.dict(os.environ, env_vars):
        # Mock the stdio_server to prevent actual server startup
        with patch('smc.server.stdio_server') as mock_stdio:
            # Create mock streams
            mock_read = AsyncMock()
            mock_write = AsyncMock()
            
            # Mock the context manager
            mock_context = AsyncMock()
            mock_context.__aenter__.return_value = (mock_read, mock_write)
            mock_context.__aexit__.return_value = None
            mock_stdio.return_value = mock_context
            
            # Mock the server.run to prevent blocking
            with patch('mcp.server.Server.run', new_callable=AsyncMock) as mock_run:
                # Run main (should not raise)
                await main()
                
                # Verify server.run was called
                mock_run.assert_called_once()


@pytest.mark.asyncio
async def test_main_cleanup_on_error():
    """Test that main() properly cleans up resources on error."""
    env_vars = {
        'MINECRAFT_PLUGIN_URL': 'http://localhost:8080',
        'MINECRAFT_API_KEY': 'test-api-key',
        'MINECRAFT_LOG_LEVEL': 'ERROR'
    }
    
    with patch.dict(os.environ, env_vars):
        # Mock stdio_server to raise an exception
        with patch('smc.server.stdio_server') as mock_stdio:
            mock_context = AsyncMock()
            mock_context.__aenter__.side_effect = RuntimeError("Test error")
            mock_stdio.return_value = mock_context
            
            # Run main and expect exception
            with pytest.raises(RuntimeError, match="Test error"):
                await main()
            
            # The test passes if cleanup happens without additional errors
