#!/bin/bash
# Build script for SMC MCP Server

set -e

echo "Building SMC MCP Server..."
echo "=========================="

# Check if uv is installed
if ! command -v uv &> /dev/null; then
    echo "Error: uv is not installed"
    echo "Please install uv: https://docs.astral.sh/uv/getting-started/installation/"
    exit 1
fi

# Run tests
echo "Running tests..."
uv run pytest tests/ -v

# Build the package
echo "Building package..."
uv build

# Check if build was successful
if [ -f "dist/smc-0.1.0-py3-none-any.whl" ] || [ -f "dist/smc-0.1.0.tar.gz" ]; then
    echo ""
    echo "✓ Build successful!"
    echo "Distribution files created in dist/"
    ls -lh dist/
    echo ""
    echo "To install:"
    echo "  uv pip install dist/smc-0.1.0-py3-none-any.whl"
    echo ""
    echo "Or install in development mode:"
    echo "  uv pip install -e ."
else
    echo ""
    echo "✗ Build failed!"
    exit 1
fi
