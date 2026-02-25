# Table Output with Pre-SQL — Kettle Plugin

Pentaho Data Integration (Kettle) 9.x 的自訂 Step Plugin。

功能與內建 **Table Output** 完全相同，額外提供一個 **Pre-SQL** 文字框，可在 INSERT 寫入資料前先執行一段 SQL（僅在 Step 啟動時執行一次）。

## 功能特色

- 繼承內建 Table Output 的所有功能（批次寫入、分區、欄位對應等）
- 新增 **Pre-SQL Tab**，支援多行 SQL 編輯
- Pre-SQL 在第一筆資料寫入前執行一次（例如 `TRUNCATE TABLE`、`DELETE FROM ... WHERE ...`）
- 支援 Kettle 變數替換（`${VAR_NAME}`）
- 多條 SQL 以分號分隔，依序執行
- Pre-SQL 留空時行為與原版 Table Output 完全相同

## 專案結構

```
kettle-plugin/
├── pom.xml
├── mvnw / mvnw.cmd                    # Maven Wrapper（免安裝 Maven）
├── src/main/java/.../tableoutputwithpresql/
│   ├── TableOutputWithPreSQLMeta.java  # 繼承 TableOutputMeta，加 preSql 欄位
│   ├── TableOutputWithPreSQL.java      # 繼承 TableOutput，加 Pre-SQL 執行邏輯
│   ├── TableOutputWithPreSQLData.java  # 繼承 TableOutputData，加 preSqlExecuted flag
│   └── TableOutputWithPreSQLDialog.java# SWT Dialog（含 Pre-SQL Tab）
├── src/main/resources/.../messages/
│   └── messages_en_US.properties       # i18n 訊息
├── src/main/resources/.../resources/
│   └── tableoutputwithpresql.svg       # Step 圖示
└── src/main/assembly/
    └── assembly.xml                    # 打包描述
```

## 環境需求

- **JDK 11**
- **Pentaho Data Integration (PDI) 9.4.0.0-343**
- Maven 3.9+（已內含 Maven Wrapper，免額外安裝）

## 打包說明

### 1. 安裝 Kettle 依賴到本機 Maven Repository

由於 Pentaho 的公開 Maven Repository 可能無法正常下載，需從本機 PDI 安裝目錄手動安裝依賴 JAR。

將以下指令中的 `F:\pdi-ce-9.4.0.0-343\data-integration` 替換為你的 PDI 安裝路徑：

```bash
# kettle-core
mvn install:install-file \
  -Dfile="F:\pdi-ce-9.4.0.0-343\data-integration\lib\kettle-core-9.4.0.0-343.jar" \
  -DgroupId=pentaho-kettle -DartifactId=kettle-core \
  -Dversion=9.4.0.0-343 -Dpackaging=jar

# kettle-engine
mvn install:install-file \
  -Dfile="F:\pdi-ce-9.4.0.0-343\data-integration\lib\kettle-engine-9.4.0.0-343.jar" \
  -DgroupId=pentaho-kettle -DartifactId=kettle-engine \
  -Dversion=9.4.0.0-343 -Dpackaging=jar

# kettle-ui-swt
mvn install:install-file \
  -Dfile="F:\pdi-ce-9.4.0.0-343\data-integration\lib\kettle-ui-swt-9.4.0.0-343.jar" \
  -DgroupId=pentaho-kettle -DartifactId=kettle-ui-swt \
  -Dversion=9.4.0.0-343 -Dpackaging=jar

# metastore
mvn install:install-file \
  -Dfile="F:\pdi-ce-9.4.0.0-343\data-integration\lib\metastore-9.4.0.0-343.jar" \
  -DgroupId=pentaho -DartifactId=metastore \
  -Dversion=9.4.0.0-343 -Dpackaging=jar
```

> 如果 `mvn` 指令找不到，可改用專案內的 `.\mvnw.cmd`（Windows）或 `./mvnw`（Linux/Mac）。

### 2. 編譯打包

```bash
.\mvnw.cmd clean package -o -DskipTests
```

參數說明：
| 參數 | 說明 |
|------|------|
| `clean` | 清除上次建置產出（刪除 `target/` 目錄） |
| `package` | 編譯原始碼並打包成 JAR |
| `-o` | 離線模式，不連遠端 Repository（避免 Pentaho repo 連線問題） |
| `-DskipTests` | 跳過單元測試，加快建置速度 |

建置成功後，JAR 檔產生在：

```
target/tableoutput-with-presql-1.0.0.jar
```

### 3. 部署到 PDI

將 JAR 複製到 PDI 的 plugins 目錄：

```bash
# 建立 plugin 目錄（首次）
mkdir "F:\pdi-ce-9.4.0.0-343\data-integration\plugins\tableoutput-with-presql-plugins"

# 複製 JAR
copy target\tableoutput-with-presql-1.0.0.jar "F:\pdi-ce-9.4.0.0-343\data-integration\plugins\tableoutput-with-presql-plugins\"
```

**重啟 Spoon** 即可在 Output 分類下看到 **Table Output with Pre-SQL**。

## 使用方式

1. 開啟 Spoon，在 Step 面板的 **Output** 分類找到 **Table Output with Pre-SQL**
2. 拖入 Transformation 畫布，雙擊開啟設定
3. **Main Tab** — 設定資料庫連線、目標表格等（與原版 Table Output 相同）
4. **Fields Tab** — 設定欄位對應（與原版 Table Output 相同）
5. **Pre-SQL Tab** — 輸入要在寫入資料前執行的 SQL，例如：
   ```sql
   TRUNCATE TABLE my_target_table;
   DELETE FROM audit_log WHERE batch_date = '${BATCH_DATE}';
   ```
6. 儲存並執行 Transformation

## 適用版本

| 項目 | 版本 |
|------|------|
| Pentaho Data Integration | 9.4.0.0-343 |
| Java | 11 |
| Maven | 3.9+（透過 Maven Wrapper） |
