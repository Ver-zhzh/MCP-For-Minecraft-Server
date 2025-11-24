# Contributing to Minecraft Server MCP

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/your-username/minecraft-server-mcp.git`
3. Create a branch: `git checkout -b feature/your-feature-name`
4. Make your changes
5. Test your changes
6. Commit: `git commit -m "Add your feature"`
7. Push: `git push origin feature/your-feature-name`
8. Create a Pull Request

## Development Setup

### Python MCP Server

```bash
# Install dependencies
pip install -e ".[dev]"

# Run tests
pytest

# Run tests with coverage
pytest --cov=src/smc
```

### Java Plugin

```bash
cd plugin
mvn clean install
mvn test
```

## Code Style

### Python
- Follow PEP 8
- Use type hints
- Write docstrings for public functions
- Keep functions focused and small

### Java
- Follow Java conventions
- Use meaningful variable names
- Add comments for complex logic
- Keep methods focused

## Testing

- Write tests for new features
- Ensure all tests pass before submitting PR
- Aim for high test coverage
- Test edge cases

## Pull Request Guidelines

1. **Title**: Use clear, descriptive titles
2. **Description**: Explain what and why
3. **Tests**: Include tests for new features
4. **Documentation**: Update docs if needed
5. **Commits**: Use clear commit messages

## Reporting Issues

When reporting issues, please include:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Environment details (OS, versions, etc.)
- Relevant logs or error messages

## Feature Requests

We welcome feature requests! Please:
- Check if it's already requested
- Explain the use case
- Describe the proposed solution
- Consider implementation complexity

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive feedback
- Help others learn and grow

## Questions?

Feel free to open an issue for questions or join discussions.

Thank you for contributing! 🎉
