"""Basic tests for the MCP server.

More tests will be added in later tasks.
"""

import pytest


def test_import():
    """Test that the smc module can be imported."""
    import smc
    assert smc.__version__ == "0.1.0"
