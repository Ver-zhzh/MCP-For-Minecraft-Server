#!/bin/bash
# Build script for SMC Plugin

set -e

echo "Building SMC Plugin..."
echo "======================"

# Navigate to plugin directory
cd "$(dirname "$0")"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Run tests
echo "Running tests..."
mvn test

# Package the plugin
echo "Packaging plugin..."
mvn package

# Check if build was successful
if [ -f "target/SMC-Plugin.jar" ]; then
    echo ""
    echo "✓ Build successful!"
    echo "Plugin JAR: target/SMC-Plugin.jar"
    echo ""
    echo "To install:"
    echo "1. Copy target/SMC-Plugin.jar to your Minecraft server's plugins/ directory"
    echo "2. Restart or reload your server"
    echo "3. Configure plugins/SMC/config.yml with your API key"
else
    echo ""
    echo "✗ Build failed!"
    exit 1
fi
