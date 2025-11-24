@echo off
REM Build script for SMC MCP Server (Windows)

echo Building SMC MCP Server...
echo ==========================

REM Check if uv is installed
where uv >nul 2>nul
if errorlevel 1 (
    echo Error: uv is not installed
    echo Please install uv: https://docs.astral.sh/uv/getting-started/installation/
    exit /b 1
)

REM Run tests
echo Running tests...
call uv run pytest tests/ -v
if errorlevel 1 goto error

REM Build the package
echo Building package...
call uv build
if errorlevel 1 goto error

REM Check if build was successful
if exist "dist\smc-0.1.0-py3-none-any.whl" (
    echo.
    echo Build successful!
    echo Distribution files created in dist\
    dir dist\
    echo.
    echo To install:
    echo   uv pip install dist\smc-0.1.0-py3-none-any.whl
    echo.
    echo Or install in development mode:
    echo   uv pip install -e .
    goto end
) else (
    goto error
)

:error
echo.
echo Build failed!
exit /b 1

:end
