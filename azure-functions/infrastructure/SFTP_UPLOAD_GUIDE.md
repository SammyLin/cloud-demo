# Azure Storage SFTP 上传指南

## 概述

本指南说明如何使用 SCP 命令通过 SFTP 上传文件到 Azure Storage Blob 容器。

## 配置信息

### 存储账户信息
- **存储账户名称**: `sftptwdmcipsdevtwncsv01`
- **SFTP 用户名**: `sftptwdmcipsdevtwnuser`
- **主机名**: `sftptwdmcipsdevtwncsv01.blob.core.windows.net`
- **目标容器**: `csv-uploads`

### SSH 密钥
- **密钥文件路径**: `~/.ssh/azure_sftp_key`
- **密钥类型**: RSA 2048-bit

## SCP 上传指令

### 基本格式
```bash
scp -i ~/.ssh/azure_sftp_key <本地文件路径> <存储账户名>.<用户名>@<主机名>:<目标文件名>
```

### 具体指令
```bash
scp -i ~/.ssh/azure_sftp_key ../test_data_valid.csv sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser@sftptwdmcipsdevtwncsv01.blob.core.windows.net:test_data_valid.csv
```

### 指令参数说明
- `-i ~/.ssh/azure_sftp_key`: 指定 SSH 私钥文件
- `../test_data_valid.csv`: 本地文件路径（相对于当前目录）
- `sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser`: 用户名格式（存储账户名.用户名）
- `@sftptwdmcipsdevtwncsv01.blob.core.windows.net`: SFTP 服务器地址
- `:test_data_valid.csv`: 目标文件名（上传到主目录）

## 其他上传示例

### 上传到子目录
```bash
# 上传到 csv-uploads 容器的子目录
scp -i ~/.ssh/azure_sftp_key ../test_data_valid.csv sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser@sftptwdmcipsdevtwncsv01.blob.core.windows.net:processed/test_data_valid.csv
```

### 批量上传
```bash
# 上传多个文件
scp -i ~/.ssh/azure_sftp_key ../*.csv sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser@sftptwdmcipsdevtwncsv01.blob.core.windows.net:
```

### 递归上传目录
```bash
# 上传整个目录
scp -i ~/.ssh/azure_sftp_key -r ../data_folder/ sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser@sftptwdmcipsdevtwncsv01.blob.core.windows.net:
```

## 验证上传

### 使用 Azure CLI 验证
```bash
# 列出容器中的文件
az storage blob list --container-name csv-uploads --account-name sftptwdmcipsdevtwncsv01

# 检查特定文件
az storage blob show --name test_data_valid.csv --container-name csv-uploads --account-name sftptwdmcipsdevtwncsv01
```

### 使用 Azure Portal 验证
1. 登录 Azure Portal
2. 导航到存储账户 `sftptwdmcipsdevtwncsv01`
3. 点击 "Containers" → "csv-uploads"
4. 查看上传的文件

## 故障排除

### 常见错误及解决方案

#### 1. 连接被拒绝
```
Received disconnect from 20.150.22.36 port 22:11: The requested container does not exist or is not accessible.
```
**解决方案**: 检查容器名称是否正确，确保 SFTP 用户有访问权限

#### 2. 权限错误
```
Permission denied (publickey)
```
**解决方案**: 检查 SSH 密钥文件路径和权限

#### 3. 文件上传失败
```
scp: remote fsetstat: Operation unsupported
```
**解决方案**: 确保使用正确的用户名格式（存储账户名.用户名）

### 调试命令
```bash
# 测试 SFTP 连接
sftp -i ~/.ssh/azure_sftp_key sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser@sftptwdmcipsdevtwncsv01.blob.core.windows.net

# 查看详细连接信息
scp -v -i ~/.ssh/azure_sftp_key ../test_data_valid.csv sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser@sftptwdmcipsdevtwncsv01.blob.core.windows.net:test_data_valid.csv
```

## 安全注意事项

1. **SSH 密钥安全**: 确保 SSH 私钥文件权限设置为 600
   ```bash
   chmod 600 ~/.ssh/azure_sftp_key
   ```

2. **文件传输**: 使用 SFTP 确保文件传输加密

3. **访问控制**: 定期轮换 SSH 密钥

4. **监控**: 启用存储账户的访问日志监控

## 自动化脚本示例

### 批量上传脚本
```bash
#!/bin/bash
# upload_csv_files.sh

SFTP_HOST="sftptwdmcipsdevtwncsv01.blob.core.windows.net"
SFTP_USER="sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser"
SSH_KEY="~/.ssh/azure_sftp_key"
SOURCE_DIR="../csv_files"
CONTAINER="csv-uploads"

echo "开始上传 CSV 文件..."

for file in $SOURCE_DIR/*.csv; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        echo "上传文件: $filename"
        scp -i $SSH_KEY "$file" "$SFTP_USER@$SFTP_HOST:$filename"
        
        if [ $? -eq 0 ]; then
            echo "✓ $filename 上传成功"
        else
            echo "✗ $filename 上传失败"
        fi
    fi
done

echo "上传完成"
```

### 使用说明
```bash
# 给脚本执行权限
chmod +x upload_csv_files.sh

# 运行脚本
./upload_csv_files.sh
```

## 相关资源

- [Azure Storage SFTP 文档](https://docs.microsoft.com/en-us/azure/storage/blobs/secure-file-transfer-protocol-support)
- [Azure CLI 存储命令](https://docs.microsoft.com/en-us/cli/azure/storage)
- [SCP 命令参考](https://man.openbsd.org/scp.1)