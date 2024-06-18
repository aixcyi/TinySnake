# 更新日志／Change Log

## v1.1.5，2024-06-18

1. 执行文档字符串相关操作时，建议用户切换到 reStructuredText 格式。  
   Suggest user that switch to reStructuredText format when performing docstring operations.
2. 生成文档字符串链接时，会从编辑器选中文本以及剪贴板中获取标题和链接；另外，优化了生成后首尾空格的插入。  
   It can retrieve text and link from selection of editor and clipboard when you open docstring link generator,
   and adjusted the insertion of leading and trailing spaces after generation.

## v1.1.4，2024-05-23

1. 插件兼容范围扩大到 `2024.2.*`，即 `242.*`。  
   Compatible with version `2024.2.*` (`242.*`).
2. 更正 `views.py` 和 `tests.py` 两个模板。  
   Swap the names of `views.py` and `tests.py` on Django app.

## v1.1.3，2024-05-09

1. 创建 Django 应用时可以使用非约定的源文件命名。  
   Allow use different file name which on django app while you creating.
2. 翻译所有对话框的「确定」和「取消」按钮。  
   Translate all dialog's buttons.
3. 更正 Django 应用创建对话框中预设的默认主键类型。  
   Correct all _default auto field_ preset on Django app creation dialog.
4. 编辑 docstring 超链接时，会校验有没有文本、是不是 HTTP(S) 链接，若校验失败会阻止插入。  
   If the text and link which want to insert into docstring is empty and not an HTTP(S) hyperlink, it will hint you and
   block inserting.
5. 光标不在 docstring 内时按下插入链接的快捷键的话，现在会弹出提示。  
   A hint will pop up if the cursor is not in docstring while you are inserting hyperlink.

## v1.1.2，2024-04-23

1. Django 应用的 `apps.py` 现在会忽略未填写的 `Meta` 属性。  
   Ignore attributes of `Meta` that you didn't fill when creating Django app.
2. Django 应用创建对话框新增了 `verbose_name` 属性，并且会自动从 `name` 中提取。  
   Add `Meta` attribute `verbose_name` on Django app creation popup, and it also will auto retrieve from app name.
3. 为 Django 应用的 `Meta` 属性输入框添加提示。  
   Add tooltips for fields of `Meta` attributes on Django app creation popup.

## v1.1.1，2024-04-20

1. 修复创建 Django 应用后缺少测试文件和迁移记录子包的问题。  
   Supplement file `tests.py` and sub-package `migrations` when you create a Django app.
2. 更正视图和路由的模板。  
   Correct templates of `views.py` and `urls.py` in Django app creation.

## v1.1.0，2024-04-19

1. 添加了一个图形界面，用于快速创建 Django App。  
   Add a UI window to fastly create Django app.
2. 打开插入 docstring 链接的弹窗时，现在会自动聚焦到输入框。  
   It will autofocus to text or link input field when you open window that insert hyperlink into docstring.

## v1.0.13，2024-04-16

1. 修复设置里 shebang 列表无法保存的问题。  
   Fixed the issue that the shebang list could not be saved in settings.
2. 更换「重置为默认」按钮的图标，优化其启用禁用逻辑。  
   Replace icon of "Reset to Default" button around shebang list, optimize its enabling and disabling logic.
3. 加入变更日志，在插件页面展示「最新变化」。  
   Add changelog and provide _What's New_ on plugin page.

## v1.0.12，2024-04-12

正式发布。  
Officially release plugin.
