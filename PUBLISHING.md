# 🚀 PyPI 发布指南 / PyPI Publishing Guide

[中文](#中文发布指南) | [English](#english-publishing-guide)

---

## 中文发布指南

### 📋 准备工作清单

在发布到 PyPI 之前，请确保完成以下步骤：

#### 1. ✏️ 更新项目信息

编辑 `pyproject.toml` 文件，修改以下信息为您的实际信息：

```toml
[project]
authors = [
    { name = "您的名字", email = "your.email@example.com" }
]

[project.urls]
Homepage = "https://github.com/您的用户名/minecraft-server-mcp"
Repository = "https://github.com/您的用户名/minecraft-server-mcp"
Issues = "https://github.com/您的用户名/minecraft-server-mcp/issues"
```

同时更新 `LICENSE` 文件中的版权信息：
```
Copyright (c) 2025 您的名字
```

#### 2. 🔍 检查项目结构

确保您的项目结构如下：

```
minecraft-server-mcp/
├── src/
│   └── smc/
│       ├── __init__.py          # 包含 __version__ = "0.1.0"
│       ├── __main__.py
│       ├── server.py
│       ├── client.py
│       ├── tools.py
│       ├── config.py
│       └── connection_manager.py
├── README.md
├── LICENSE
├── pyproject.toml
└── MANIFEST.in
```

#### 3. 🧪 本地测试

在发布之前，先在本地测试打包：

```bash
# 安装构建工具
uv pip install build twine

# 或使用 pip
pip install build twine

# 清理旧的构建文件
rm -rf dist/ build/ *.egg-info

# 构建包
python -m build
```

这将在 `dist/` 目录中生成两个文件：
- `minecraft_server_mcp-0.1.0-py3-none-any.whl` (wheel 格式)
- `minecraft_server_mcp-0.1.0.tar.gz` (源码包)

#### 4. ✅ 验证打包内容

检查打包的文件：

```bash
# 检查 wheel 内容
unzip -l dist/minecraft_server_mcp-0.1.0-py3-none-any.whl

# 检查 tar.gz 内容
tar -tzf dist/minecraft_server_mcp-0.1.0.tar.gz

# 使用 twine 检查包的元数据
twine check dist/*
```

确保：
- ✅ 所有 Python 源文件都被包含
- ✅ README.md 和 LICENSE 文件被包含
- ✅ 没有包含不必要的文件（如 .git, __pycache__ 等）
- ✅ 通过 twine 检查，没有警告和错误

---

### 🎯 发布到 PyPI

#### 方法一：使用 TestPyPI（推荐首次发布）

TestPyPI 是 PyPI 的测试环境，建议先在这里测试发布流程。

1️⃣ **注册 TestPyPI 账号**
   - 访问 https://test.pypi.org/account/register/
   - 完成注册并验证邮箱

2️⃣ **创建 API Token**
   - 登录后访问 https://test.pypi.org/manage/account/#api-tokens
   - 点击 "Add API token"
   - Token 名称：`minecraft-mcp-upload`
   - 范围：选择 "Entire account" 或创建后选择具体项目
   - **重要**：复制生成的 token（格式为 `pypi-...`），它只会显示一次！

3️⃣ **配置凭证（可选）**

创建或编辑 `~/.pypirc` 文件：

```ini
[distutils]
index-servers =
    testpypi
    pypi

[testpypi]
repository = https://test.pypi.org/legacy/
username = __token__
password = pypi-你的TestPyPI_Token

[pypi]
repository = https://upload.pypi.org/legacy/
username = __token__
password = pypi-你的PyPI_Token
```

**安全提示**：在 Windows 上，该文件位于 `C:\Users\你的用户名\.pypirc`

4️⃣ **上传到 TestPyPI**

```bash
twine upload --repository testpypi dist/*

# 或者直接使用 token（无需 .pypirc）
twine upload --repository testpypi -u __token__ -p pypi-你的Token dist/*
```

5️⃣ **测试安装**

```bash
# 从 TestPyPI 安装测试
pip install --index-url https://test.pypi.org/simple/ --extra-index-url https://pypi.org/simple/ minecraft-server-mcp

# 测试运行
minecraft-mcp --help
```

---

#### 方法二：发布到正式 PyPI

**⚠️ 警告**：发布到 PyPI 后**无法删除**，只能发布新版本！请确保先在 TestPyPI 测试成功。

1️⃣ **注册 PyPI 账号**
   - 访问 https://pypi.org/account/register/
   - 完成注册并验证邮箱

2️⃣ **创建 API Token**
   - 登录后访问 https://pypi.org/manage/account/#api-tokens
   - 创建 token（步骤同 TestPyPI）

3️⃣ **上传到 PyPI**

```bash
# 使用 .pypirc 中的配置
twine upload dist/*

# 或直接使用 token
twine upload -u __token__ -p pypi-你的PyPI_Token dist/*
```

4️⃣ **验证发布**

- 访问 https://pypi.org/project/minecraft-server-mcp/
- 检查项目页面显示是否正确
- 测试安装：`pip install minecraft-server-mcp`

---

### 🔄 发布新版本

当您需要发布新版本时：

1️⃣ **更新版本号**

编辑 `src/smc/__init__.py`:
```python
__version__ = "0.1.1"  # 或 0.2.0, 1.0.0 等
```

编辑 `pyproject.toml`:
```toml
[project]
version = "0.1.1"
```

2️⃣ **更新 CHANGELOG**（推荐）

创建 `CHANGELOG.md` 记录版本变更。

3️⃣ **重新构建和发布**

```bash
# 清理旧版本
rm -rf dist/ build/

# 重新构建
python -m build

# 检查
twine check dist/*

# 上传
twine upload dist/*
```

---

### 📝 版本号规范

遵循 [语义化版本 (Semantic Versioning)](https://semver.org/lang/zh-CN/)：

- **主版本号 (MAJOR)**：不兼容的 API 修改
- **次版本号 (MINOR)**：向下兼容的功能性新增
- **修订号 (PATCH)**：向下兼容的问题修正

示例：
- `0.1.0` → `0.1.1`：修复 bug
- `0.1.1` → `0.2.0`：新增功能
- `0.2.0` → `1.0.0`：首个稳定版本或重大变更

---

### 🛡️ 最佳实践

1. **使用 Git 标签**
   ```bash
   git tag -a v0.1.0 -m "Release version 0.1.0"
   git push origin v0.1.0
   ```

2. **自动化发布流程**（可选）
   - 使用 GitHub Actions 自动构建和发布
   - 创建 `.github/workflows/publish.yml`

3. **维护 CHANGELOG.md**
   - 记录每个版本的变更
   - 方便用户了解更新内容

4. **测试覆盖率**
   - 确保有充分的单元测试
   - 使用 `pytest --cov` 检查覆盖率

---

### ❓ 常见问题

**Q: 上传失败，提示 "File already exists"**

A: PyPI 不允许重新上传相同版本。请更新版本号后重新构建上传。

**Q: 如何删除已发布的包？**

A: PyPI 不允许删除包，但可以：
- "yank" 版本（隐藏但仍可安装）
- 发布新版本修复问题

**Q: 包名已被占用怎么办？**

A: 修改 `pyproject.toml` 中的 `name` 字段，例如：
```toml
name = "minecraft-mcp-server"  # 或其他可用名称
```

---

## English Publishing Guide

### 📋 Pre-release Checklist

Before publishing to PyPI, make sure to complete these steps:

#### 1. ✏️ Update Project Information

Edit `pyproject.toml` and update with your actual information:

```toml
[project]
authors = [
    { name = "Your Name", email = "your.email@example.com" }
]

[project.urls]
Homepage = "https://github.com/your-username/minecraft-server-mcp"
Repository = "https://github.com/your-username/minecraft-server-mcp"
Issues = "https://github.com/your-username/minecraft-server-mcp/issues"
```

Also update `LICENSE` file copyright:
```
Copyright (c) 2025 Your Name
```

#### 2. 🔍 Verify Project Structure

Ensure your project structure looks like this:

```
minecraft-server-mcp/
├── src/
│   └── smc/
│       ├── __init__.py          # Contains __version__ = "0.1.0"
│       ├── __main__.py
│       ├── server.py
│       ├── client.py
│       ├── tools.py
│       ├── config.py
│       └── connection_manager.py
├── README.md
├── LICENSE
├── pyproject.toml
└── MANIFEST.in
```

#### 3. 🧪 Local Testing

Test the package build locally before publishing:

```bash
# Install build tools
uv pip install build twine

# Or using pip
pip install build twine

# Clean old builds
rm -rf dist/ build/ *.egg-info

# Build the package
python -m build
```

This will generate two files in `dist/`:
- `minecraft_server_mcp-0.1.0-py3-none-any.whl` (wheel format)
- `minecraft_server_mcp-0.1.0.tar.gz` (source distribution)

#### 4. ✅ Verify Package Contents

Check the packaged files:

```bash
# Check wheel contents
unzip -l dist/minecraft_server_mcp-0.1.0-py3-none-any.whl

# Check tar.gz contents
tar -tzf dist/minecraft_server_mcp-0.1.0.tar.gz

# Verify package metadata with twine
twine check dist/*
```

Ensure:
- ✅ All Python source files are included
- ✅ README.md and LICENSE are included
- ✅ No unnecessary files (like .git, __pycache__, etc.)
- ✅ Passes twine check without warnings or errors

---

### 🎯 Publishing to PyPI

#### Method 1: Using TestPyPI (Recommended for First-time Publishers)

TestPyPI is PyPI's test environment. It's recommended to test your release process here first.

1️⃣ **Register TestPyPI Account**
   - Visit https://test.pypi.org/account/register/
   - Complete registration and verify email

2️⃣ **Create API Token**
   - After logging in, go to https://test.pypi.org/manage/account/#api-tokens
   - Click "Add API token"
   - Token name: `minecraft-mcp-upload`
   - Scope: Choose "Entire account" or select specific project after creation
   - **Important**: Copy the generated token (format: `pypi-...`), it only shows once!

3️⃣ **Configure Credentials (Optional)**

Create or edit `~/.pypirc`:

```ini
[distutils]
index-servers =
    testpypi
    pypi

[testpypi]
repository = https://test.pypi.org/legacy/
username = __token__
password = pypi-YourTestPyPI_Token

[pypi]
repository = https://upload.pypi.org/legacy/
username = __token__
password = pypi-YourPyPI_Token
```

**Security Note**: On Windows, this file is located at `C:\Users\YourUsername\.pypirc`

4️⃣ **Upload to TestPyPI**

```bash
twine upload --repository testpypi dist/*

# Or use token directly (no .pypirc needed)
twine upload --repository testpypi -u __token__ -p pypi-YourToken dist/*
```

5️⃣ **Test Installation**

```bash
# Install from TestPyPI
pip install --index-url https://test.pypi.org/simple/ --extra-index-url https://pypi.org/simple/ minecraft-server-mcp

# Test run
minecraft-mcp --help
```

---

#### Method 2: Publishing to Production PyPI

**⚠️ Warning**: Once published to PyPI, packages **cannot be deleted**, only new versions can be released! Make sure to test on TestPyPI first.

1️⃣ **Register PyPI Account**
   - Visit https://pypi.org/account/register/
   - Complete registration and verify email

2️⃣ **Create API Token**
   - After logging in, go to https://pypi.org/manage/account/#api-tokens
   - Create token (same steps as TestPyPI)

3️⃣ **Upload to PyPI**

```bash
# Using .pypirc configuration
twine upload dist/*

# Or use token directly
twine upload -u __token__ -p pypi-YourPyPI_Token dist/*
```

4️⃣ **Verify Release**

- Visit https://pypi.org/project/minecraft-server-mcp/
- Check if project page displays correctly
- Test installation: `pip install minecraft-server-mcp`

---

### 🔄 Releasing New Versions

When releasing a new version:

1️⃣ **Update Version Number**

Edit `src/smc/__init__.py`:
```python
__version__ = "0.1.1"  # or 0.2.0, 1.0.0, etc.
```

Edit `pyproject.toml`:
```toml
[project]
version = "0.1.1"
```

2️⃣ **Update CHANGELOG** (Recommended)

Create `CHANGELOG.md` to track version changes.

3️⃣ **Rebuild and Publish**

```bash
# Clean old versions
rm -rf dist/ build/

# Rebuild
python -m build

# Check
twine check dist/*

# Upload
twine upload dist/*
```

---

### 📝 Version Numbering Convention

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Incompatible API changes
- **MINOR**: Backward-compatible functionality additions
- **PATCH**: Backward-compatible bug fixes

Examples:
- `0.1.0` → `0.1.1`: Bug fixes
- `0.1.1` → `0.2.0`: New features
- `0.2.0` → `1.0.0`: First stable release or major changes

---

### 🛡️ Best Practices

1. **Use Git Tags**
   ```bash
   git tag -a v0.1.0 -m "Release version 0.1.0"
   git push origin v0.1.0
   ```

2. **Automate Release Process** (Optional)
   - Use GitHub Actions for automated builds and publishing
   - Create `.github/workflows/publish.yml`

3. **Maintain CHANGELOG.md**
   - Record changes for each version
   - Help users understand updates

4. **Test Coverage**
   - Ensure adequate unit tests
   - Use `pytest --cov` to check coverage

---

### ❓ FAQ

**Q: Upload failed with "File already exists" error**

A: PyPI doesn't allow re-uploading the same version. Update version number and rebuild.

**Q: How to delete a published package?**

A: PyPI doesn't allow deletion, but you can:
- "Yank" versions (hide but still installable)
- Publish new version to fix issues

**Q: Package name already taken?**

A: Modify `name` field in `pyproject.toml`, e.g.:
```toml
name = "minecraft-mcp-server"  # or another available name
```

---

## 🎉 快速发布命令 / Quick Release Commands

**中文版本:**
```bash
# 1. 更新版本号（编辑 pyproject.toml 和 __init__.py）
# 2. 清理并构建
rm -rf dist/ build/ && python -m build
# 3. 检查包
twine check dist/*
# 4. 先发布到 TestPyPI 测试
twine upload --repository testpypi dist/*
# 5. 测试通过后发布到正式 PyPI
twine upload dist/*
```

**English Version:**
```bash
# 1. Update version (edit pyproject.toml and __init__.py)
# 2. Clean and build
rm -rf dist/ build/ && python -m build
# 3. Check package
twine check dist/*
# 4. Upload to TestPyPI first
twine upload --repository testpypi dist/*
# 5. After testing, upload to production PyPI
twine upload dist/*
```
