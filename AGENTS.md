# AGENTS.md - PocketLedger 项目工作规则

## 基本原则

- 默认使用中文回复、中文注释和中文文档；代码标识符、框架 API、错误原文按上下文保留英文。
- 修改前先阅读相关调用方、状态管理、数据模型和现有 UI 约定，不凭猜测改代码。
- 只做满足当前目标的最小改动，避免顺手重构无关模块。
- 遵循现有 Kotlin、Jetpack Compose、ViewModel、StateFlow 和仓库结构风格。
- 已有未提交改动默认视为用户改动，不回退、不覆盖，除非用户明确要求。

## 验证要求

- 涉及 Kotlin/Compose 代码改动时，优先运行 `.\gradlew.bat :app:compileDebugKotlin` 验证编译。
- 涉及业务逻辑、数据转换或边界条件时，补充相应测试或说明未补测试原因。
- 完成时明确说明已验证内容、未验证内容和残留风险。

## 输入型弹窗草稿缓存约定

- 新增或修改任何包含用户输入的弹窗，包括 `ModalBottomSheet`、`AlertDialog` 或自定义弹窗，默认必须保留未保存输入。
- 用户通过下滑、点击遮罩、返回键、关闭按钮或切换页面临时关闭弹窗时，不应清空草稿。
- 再次打开同一个新建弹窗时，应恢复上一次未保存的输入内容。
- 保存成功、删除成功、明确点击“清空/重置”，或业务上确认放弃草稿时，才可以清空对应输入缓存。
- 编辑已有记录时可以加载记录本身的数据；从编辑态切回新建态时，不能把旧记录内容误带到新建弹窗。
- 草稿优先放在 `ViewModel` 的 UI state 中管理；仅组件内部临时输入且不影响业务状态时，可提升到父级 `rememberSaveable`，避免弹窗销毁后丢失。
- 一个弹窗内的附属输入框也要遵守缓存约定，例如“新增成员”“新增提醒”“新增清单项”等尚未提交的小输入框。

## 当前实现参考

- 账单新增弹窗草稿：`BillViewModel.openNewBill` 和 `EditorState.draft`。
- AA 新建/支出弹窗草稿：`AaUiState.draft`、`AaUiState.expenseDraft`。
- 旅行支出/旅行计划弹窗草稿：`TravelUiState.draft`、`TravelTripCreatorState.draft`。
- 弹窗内临时成员输入缓存：`AaSplitScreen` 与 `TravelScreen` 中提升到父级的 `rememberSaveable` 状态。
