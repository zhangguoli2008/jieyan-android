# QuitBuddy Android

QuitBuddy 是一款面向 Android 平台的戒烟辅助应用，核心功能聚焦快速记录渴求、提供微干预工具、展示戒烟收益并通过提醒和勋章维持动力。项目完全采用 Java 构建，离线可用，支持 Android 8.0（API 26）及以上版本。

## 功能总览

- **Onboarding**：首次启动收集起始日、每日基线、价格与提醒时段，生成戒烟计划。
- **仪表盘**：展示无烟天数、节省金额、避免香烟与回收时间，可快捷进入渴求记录、微干预、成就、导出与设置。
- **渴求记录**：支持强度、触发、是否吸烟及备注的快速录入，并提供历史列表。
- **微干预工具**：包含 4-4-4-4 呼吸练习、3 分钟延迟计时、随机替代建议。
- **通知体系**：固定提醒、里程碑提醒与基于 7 日数据的高风险预测提醒，均通过本地通知触达。
- **成就与勋章**：跟踪阶段性天数与连续未吸指标。
- **数据导出**：一键导出 CSV 文件到应用专属目录。
- **设置**：主题切换、云同步开关（示例实现）与数据导出入口。
- **桌面小组件**：展示实时无烟天数与节省金额。

详尽的 V1.0 需求说明请参阅 [`docs/requirements_v1.md`](docs/requirements_v1.md)。

## 构建与运行

1. 使用 Android Studio (Giraffe 以上版本) 打开仓库根目录。
2. 同步 Gradle，必要时允许 IDE 自动下载 Android SDK 组件。
3. 选择连接的设备或模拟器（Android 8.0+），点击 **Run** 即可安装运行。

### 命令行构建

```bash
# 首次可运行一次 gradle wrapper 生成本地 wrapper
gradle wrapper
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/`。

## 目录结构

```
app/
  src/main/java/com/quitbuddy/
    app/                // Application 入口
    data/               // Room 数据库与仓库层
    notifications/      // 通知与调度工具
    ui/                 // Activity/Fragment 与界面逻辑
    widget/             // App Widget 提供器
  src/main/res/         // 布局、主题、字符串等资源
docs/requirements_v1.md // 详细需求文档
```
