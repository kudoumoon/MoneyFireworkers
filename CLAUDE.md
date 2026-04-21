# CLAUDE.md

## Project Intent

MoneyFireworkers 是一个 Android 支出记账应用，强调温柔、轻松、治愈、有陪伴感的产品气质。

核心产品模型：

- 用户设置本月预期支出
- 系统记录实际支出
- 呼吸感 = 预期支出 - 实际支出
- 呼吸感作为首页核心视觉

## Current Product Scope

- 仅记录支出，不做收入系统
- 手动录入支出可用
- 首页、添加、统计、分类、设置 5 Tab 可用
- 微信通知识别模块已接入第一版

## Engineering Rules

- Kotlin + Jetpack Compose + Room
- 持久化优先，避免只靠内存状态
- 通知识别必须只生成候选卡片，不可直接自动入账
- 不使用 AccessibilityService 作为主方案
- 解析、分类、确认要分层
- 所有识别结果必须写日志，便于后续调试

## Important Modules

- `app/AppContainer.kt`
  - 负责仓储和 use case 装配
- `app/MainActivity.kt`
  - 根导航、底部 Tab、通知候选卡片弹层
- `feature/dashboard/DashboardRoute.kt`
  - 首页与呼吸感主卡
- `notification/service/WeChatExpenseNotificationListenerService.kt`
  - 微信通知监听入口
- `domain/usecase/notification/ProcessWeChatNotificationUseCase.kt`
  - 通知识别业务编排

## Notification Recognition Flow

1. `NotificationListenerService` 收到微信通知
2. `WeChatPaymentNotificationParser` 提取金额、商户、时间
3. `ProcessWeChatNotificationUseCase` 进行：
   - 规则分类
   - 候选账单创建
   - pending action 创建
   - 日志写库
4. `NotificationCandidateCoordinator` 把候选卡发给前台
5. `MainActivity` 弹出确认卡片
6. 用户确认、改分类确认或忽略

## Release Bar

只有满足以下条件才算可发售：

- 首页呼吸感三态明确且观感稳定
- 底部系统栏无明显黑底或破坏观感的问题
- 首页账单能点进详情
- 微信通知识别不会直接自动入账
- 候选卡支持确认、改分类确认、忽略
- 解析失败有手动补录兜底
- 识别日志可追踪
- 关键改动至少完成一次真机构建验证
