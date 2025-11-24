#!/bin/bash
# Quick start script for SMC - builds and provides installation instructions

set -e

echo "=========================================="
echo "SMC - Minecraft Server MCP Quick Start"
echo "=========================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 8 or higher."
    exit 1
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo "✓ Java found (version $JAVA_VERSION)"
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6 or higher."
    exit 1
else
    echo "✓ Maven found"
fi

# Check Python
if ! command -v python3 &> /dev/null && ! command -v python &> /dev/null; then
    echo "❌ Python not found. Please install Python 3.12 or higher."
    exit 1
else
    echo "✓ Python found"
fi

# Check uv
if ! command -v uv &> /dev/null; then
    echo "⚠ uv not found. Install it for better performance:"
    echo "  curl -LsSf https://astral.sh/uv/install.sh | sh"
    echo ""
fi

echo ""
echo "Building components..."
echo "=========================================="

# Build Java plugin
echo ""
echo "1. Building Java plugin..."
cd plugin
mvn clean package -DskipTests
if [ $? -eq 0 ]; then
    echo "✓ Java plugin built successfully"
    PLUGIN_JAR="$(pwd)/target/SMC-Plugin.jar"
else
    echo "❌ Java plugin build failed"
    exit 1
fi
cd ..

# Build Python MCP server
echo ""
echo "2. Building Python MCP server..."
if command -v uv &> /dev/null; then
    uv build
else
    python -m build
fi

if [ $? -eq 0 ]; then
    echo "✓ Python MCP server built successfully"
    PYTHON_WHEEL="$(pwd)/dist/smc-0.1.0-py3-none-any.whl"
else
    echo "❌ Python MCP server build failed"
    exit 1
fi

echo ""
echo "=========================================="
echo "✓ Build Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. Install the Java plugin:"
echo "   Copy this file to your Minecraft server's plugins/ directory:"
echo "   $PLUGIN_JAR"
echo ""
echo "2. Configure the plugin:"
echo "   Edit plugins/SMC/config.yml and set a secure API key"
echo ""
echo "3. Install the Python MCP server:"
if command -v uv &> /dev/null; then
    echo "   uv pip install $PYTHON_WHEEL"
else
    echo "   pip install $PYTHON_WHEEL"
fi
echo ""
echo "4. Set environment variables:"
echo "   export MINECRAFT_PLUGIN_URL=http://localhost:8080"
echo "   export MINECRAFT_API_KEY=your-api-key-here"
echo ""
echo "5. Configure your MCP client (e.g., ~/.kiro/settings/mcp.json):"
echo '   {'
echo '     "mcpServers": {'
echo '       "minecraft": {'
echo '         "command": "smc",'
echo '         "env": {'
echo '           "MINECRAFT_PLUGIN_URL": "http://localhost:8080",'
echo '           "MINECRAFT_API_KEY": "your-api-key-here"'
echo '         }'
echo '       }'
echo '     }'
echo '   }'
echo ""
echo "For detailed instructions, see INSTALL.md"
echo ""
