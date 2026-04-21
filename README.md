# MoneyFireworkers

一个偏温柔、有人情味的 Android 支出记账应用。

当前版本聚焦 3 件事：

- 快速记录支出
- 用“呼吸感”理解本月预算空间
- 通过微信通知识别生成候选支出卡，再由用户确认

## 当前功能

- 首页显示本月呼吸感、已花、预算、待确认支出
- 呼吸感主卡支持三种视觉状态
  - 绿色：预算充裕
  - 黄色：开始变紧
  - 红色：逼近或已经超支
- 添加页支持手动录入支出
- 统计页支持查看趋势、分类占比、情绪占比
- 分类页支持维护支出分类
- 设置页支持预算、提醒、导出、语言切换
- 支持点击首页账单进入详情

## 微信通知识别模块

当前采用 `NotificationListenerService`，不依赖 `AccessibilityService` 作为主方案。

流程如下：

1. 用户手动授权通知读取权限
2. 服务仅过滤 `com.tencent.mm`
3. 解析通知文本中的金额、商户、时间
4. 走分类规则引擎预测分类
5. 只生成候选支出卡，不直接入账
6. 前台弹出确认卡片，支持：
   - 确认
   - 改分类后确认
   - 忽略
7. 所有识别结果写入本地日志表 `notification_recognition_logs`
8. 如果解析失败，转为手动补录兜底

## 关键架构

### UI 层

- `app/MainActivity.kt`
- `feature/dashboard`
- `feature/addexpense`
- `feature/stats`
- `feature/category`
- `feature/profile`

### 业务层

- `domain/usecase/expense`
- `domain/usecase/pending`
- `domain/usecase/process`
- `domain/usecase/notification`

### 通知识别层

- `notification/service/WeChatExpenseNotificationListenerService.kt`
- `notification/parse/WeChatPaymentNotificationParser.kt`
- `notification/coordination/NotificationCandidateCoordinator.kt`

### 数据层

- Room 数据库：`AppDatabase`
- 本地日志表：`notification_recognition_logs`
- 分类规则表：`classification_rules`
- 账单表：`ledger_entries`
- 事件表：`payment_events`
- 待确认表：`pending_actions`

## 本地运行

### 环境

- Android Studio
- JDK 17
- Android SDK 34
- Gradle 8.7

### 构建

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;C:\Users\27665\Tools\gradle-8.7\bin;$env:Path"
gradle assembleDebug
```

### 安装到手机

```powershell
C:\Users\27665\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

## 开启微信通知识别

1. 打开系统设置
2. 找到“通知使用权”或“通知访问权限”
3. 给 `MoneyFireworkers` 授权
4. 保持微信通知可正常显示

说明：

- 当前只监听微信通知
- 当前只生成候选卡片，不会绕过用户直接记账
- 如果通知格式变化，优先通过日志修正规则与解析器

## 下一步建议

- 给通知识别加一页“权限状态与调试日志”
- 补更多微信通知样本规则
- 把候选卡片补充为可编辑金额/备注
- 增加通知识别相关单元测试
