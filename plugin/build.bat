@echo off
REM Build script for SMC Plugin (Windows)

echo Building SMC Plugin...
echo ======================

REM Navigate to plugin directory
cd /d "%~dp0"

REM Clean previous builds
echo Cleaning previous builds...
call mvn clean
if errorlevel 1 goto error

REM Run tests
echo Running tests...
call mvn test
if errorlevel 1 goto error

REM Package the plugin
echo Packaging plugin...
call mvn package
if errorlevel 1 goto error

REM Check if build was successful
if exist "target\SMC-Plugin.jar" (
    echo.
    echo Build successful!
    echo Plugin JAR: target\SMC-Plugin.jar
    echo.
    echo To install:
    echo 1. Copy target\SMC-Plugin.jar to your Minecraft server's plugins\ directory
    echo 2. Restart or reload your server
    echo 3. Configure plugins\SMC\config.yml with your API key
    goto end
) else (
    goto error
)

:error
echo.
echo Build failed!
exit /b 1

:end
