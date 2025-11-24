"""Entry point for the MCP server."""

import asyncio
import sys
from .server import main


def cli_main():
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nMCP server stopped by user", file=sys.stderr)
        sys.exit(0)
    except Exception as e:
        print(f"Fatal error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    cli_main()
