#!/bin/bash

# Azure Storage SFTP 上傳腳本
# 使用方法: ./upload_csv.sh [檔案名1] [檔案名2] ...

# 配置資訊
SFTP_HOST="sftptwdmcipsdevtwncsv01.blob.core.windows.net"
SFTP_USER="sftptwdmcipsdevtwncsv01.sftptwdmcipsdevtwnuser"
SSH_KEY="~/.ssh/azure_sftp_key"
UPLOAD_DIR="csv-uploads"

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 列印帶顏色的訊息
print_info() {
    echo -e "${BLUE}[資訊]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[成功]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[警告]${NC} $1"
}

print_error() {
    echo -e "${RED}[錯誤]${NC} $1"
}

# 顯示說明資訊
show_help() {
    echo "Azure Storage SFTP CSV 檔案上傳腳本"
    echo ""
    echo "使用方法:"
    echo "  $0 <檔案名1> [檔案名2] ..."
    echo "  $0 --interactive"
    echo "  $0 --list"
    echo "  $0 --help"
    echo ""
    echo "選項:"
    echo "  --interactive    互動式選擇檔案"
    echo "  --list          列出當前目錄的 CSV 檔案"
    echo "  --help          顯示此說明資訊"
    echo ""
    echo "範例:"
    echo "  $0 test_data_valid.csv"
    echo "  $0 test_data_valid.csv test_data_invalid.csv"
    echo "  $0 --interactive"
}

# 列出當前目錄的 CSV 檔案
list_csv_files() {
    print_info "當前目錄的 CSV 檔案:"
    local csv_files=($(ls -1 *.csv 2>/dev/null))
    
    if [ ${#csv_files[@]} -eq 0 ]; then
        print_warning "當前目錄沒有找到 CSV 檔案"
        return 1
    fi
    
    for i in "${!csv_files[@]}"; do
        echo "  $((i+1)). ${csv_files[$i]}"
    done
    echo ""
}

# 互動式選擇檔案
interactive_mode() {
    print_info "進入互動式模式..."
    
    # 列出 CSV 檔案
    local csv_files=($(ls -1 *.csv 2>/dev/null))
    
    if [ ${#csv_files[@]} -eq 0 ]; then
        print_error "當前目錄沒有找到 CSV 檔案"
        exit 1
    fi
    
    echo "請選擇要上傳的檔案 (輸入數字，多個檔案用空格分隔，輸入 'all' 上傳所有檔案):"
    for i in "${!csv_files[@]}"; do
        echo "  $((i+1)). ${csv_files[$i]}"
    done
    echo "  all. 上傳所有檔案"
    echo ""
    
    read -p "請輸入選擇: " selection
    
    if [ "$selection" = "all" ]; then
        upload_files "${csv_files[@]}"
    else
        local selected_files=()
        for num in $selection; do
            if [[ "$num" =~ ^[0-9]+$ ]] && [ "$num" -ge 1 ] && [ "$num" -le ${#csv_files[@]} ]; then
                selected_files+=("${csv_files[$((num-1))]}")
            else
                print_warning "無效的選擇: $num"
            fi
        done
        
        if [ ${#selected_files[@]} -gt 0 ]; then
            upload_files "${selected_files[@]}"
        else
            print_error "沒有選擇有效的檔案"
            exit 1
        fi
    fi
}

# 檢查 SSH 金鑰
check_ssh_key() {
    local key_path=$(eval echo $SSH_KEY)
    if [ ! -f "$key_path" ]; then
        print_error "SSH 金鑰檔案不存在: $key_path"
        print_info "請確保 SSH 金鑰已正確配置"
        exit 1
    fi
    
    # 設定正確的權限
    chmod 600 "$key_path" 2>/dev/null
}

# 上傳單個檔案
upload_single_file() {
    local file_path="$1"
    local filename=$(basename "$file_path")
    
    print_info "開始上傳檔案: $filename"
    print_info "目標: $SFTP_USER@$SFTP_HOST"
    
    # 執行 SCP 上傳 - 直接上傳到主目錄
    scp -i $SSH_KEY "$file_path" "$SFTP_USER@$SFTP_HOST:$filename"
    
    if [ $? -eq 0 ]; then
        print_success "檔案 '$filename' 上傳成功"
        print_info "檔案位置: csv-uploads/$filename"
        return 0
    else
        print_error "檔案 '$filename' 上傳失敗"
        return 1
    fi
}

# 上傳多個檔案
upload_files() {
    local files=("$@")
    local success_count=0
    local total_count=${#files[@]}
    
    print_info "準備上傳 $total_count 個檔案..."
    
    for file_path in "${files[@]}"; do
        if [ ! -f "$file_path" ]; then
            print_error "檔案不存在: $file_path"
            continue
        fi
        
        if upload_single_file "$file_path"; then
            ((success_count++))
        fi
        
        echo ""
    done
    
    print_info "上傳完成: $success_count/$total_count 個檔案成功"
    
    if [ $success_count -eq $total_count ]; then
        print_success "所有檔案上傳成功！"
        print_info "檔案將在幾分鐘內被自動處理"
        print_info "您可以透過以下 API 檢查處理狀態:"
        print_info "  - 查詢資料: GET /api/data"
        print_info "  - 查詢特定檔案: GET /api/query?fileName=$(basename "${files[0]}" .csv)"
    else
        print_warning "部分檔案上傳失敗，請檢查錯誤訊息"
    fi
}

# 主程式
main() {
    # 檢查參數
    if [ $# -eq 0 ]; then
        show_help
        exit 1
    fi
    
    # 處理特殊參數
    case "$1" in
        --help)
            show_help
            exit 0
            ;;
        --list)
            list_csv_files
            exit 0
            ;;
        --interactive)
            check_ssh_key
            interactive_mode
            exit 0
            ;;
    esac
    
    # 檢查 SSH 金鑰
    check_ssh_key
    
    # 上傳指定的檔案
    upload_files "$@"
}

# 執行主程式
main "$@" 