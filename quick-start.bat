@echo off
REM Quick start script for SMC - builds and provides installation instructions

echo ==========================================
echo SMC - Minecraft Server MCP Quick Start
echo ==========================================
echo.

REM Check prerequisites
echo Checking prerequisites...

REM Check Java
where java >nul 2>nul
if errorlevel 1 (
    echo X Java not found. Please install Java 8 or higher.
    exit /b 1
) else (
    echo √ Java found
)

REM Check Maven
where mvn >nul 2>nul
if errorlevel 1 (
    echo X Maven not found. Please install Maven 3.6 or higher.
    exit /b 1
) else (
    echo √ Maven found
)

REM Check Python
where python >nul 2>nul
if errorlevel 1 (
    echo X Python not found. Please install Python 3.12 or higher.
    exit /b 1
) else (
    echo √ Python found
)

REM Check uv
where uv >nul 2>nul
if errorlevel 1 (
    echo ! uv not found. Install it for better performance:
    echo   powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
    echo.
)

echo.
echo Building components...
echo ==========================================

REM Build Java plugin
echo.
echo 1. Building Java plugin...
cd plugin
call mvn clean package -DskipTests
if errorlevel 1 (
    echo X Java plugin build failed
    exit /b 1
)
echo √ Java plugin built successfully
set PLUGIN_JAR=%CD%\target\SMC-Plugin.jar
cd ..

REM Build Python MCP server
echo.
echo 2. Building Python MCP server...
where uv >nul 2>nul
if errorlevel 1 (
    python -m build
) else (
    call uv build
)

if errorlevel 1 (
    echo X Python MCP server build failed
    exit /b 1
)
echo √ Python MCP server built successfully
set PYTHON_WHEEL=%CD%\dist\smc-0.1.0-py3-none-any.whl

echo.
echo ==========================================
echo √ Build Complete!
echo ==========================================
echo.
echo Next steps:
echo.
echo 1. Install the Java plugin:
echo    Copy this file to your Minecraft server's plugins\ directory:
echo    %PLUGIN_JAR%
echo.
echo 2. Configure the plugin:
echo    Edit plugins\SMC\config.yml and set a secure API key
echo.
echo 3. Install the Python MCP server:
where uv >nul 2>nul
if errorlevel 1 (
    echo    pip install %PYTHON_WHEEL%
) else (
    echo    uv pip install %PYTHON_WHEEL%
)
echo.
echo 4. Set environment variables:
echo    set MINECRAFT_PLUGIN_URL=http://localhost:8080
echo    set MINECRAFT_API_KEY=your-api-key-here
echo.
echo 5. Configure your MCP client (e.g., ~/.kiro/settings/mcp.json):
echo    {
echo      "mcpServers": {
echo        "minecraft": {
echo          "command": "smc",
echo          "env": {
echo            "MINECRAFT_PLUGIN_URL": "http://localhost:8080",
echo            "MINECRAFT_API_KEY": "your-api-key-here"
echo          }
echo        }
echo      }
echo    }
echo.
echo For detailed instructions, see INSTALL.md
echo.
