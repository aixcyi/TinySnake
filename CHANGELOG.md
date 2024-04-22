# 更新日志／Change Log

## v1.1.2，2024-04-23

1. Django App 的 `apps.py` 现在会忽略未填写的 `Meta` 属性。  
   Ignore attributes of `Meta` that you didn't fill when creating Django app.
2. Django App 创建对话框新增了 `verbose_name` 属性，并且会自动从 `name` 中提取。  
   Add `Meta` attribute `verbose_name` on Django app creation popup, and it also will auto retrieve from app name.
3. 为 Django App 的 `Meta` 属性输入框添加提示。
   Add tooltips for fields of `Meta` attributes on Django app creation popup.

## v1.1.1，2024-04-20

1. 修复创建 Django App 后缺少测试文件和迁移记录子包的问题。  
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
