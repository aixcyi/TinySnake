# Tiny Snake

[![开始使用](https://img.shields.io/badge/开始使用-中文-blue.svg?style=flat-square)](./README.md)
[![Getting Started](https://img.shields.io/badge/Getting%20Started-English-green.svg?style=flat-square)](./README_en.md)

A tiny plugin to improve Python development experience.

## Features

- Generate variable `__all__`, sort it list, change quotes and lines style, etc.
- Insert hyperlink into [reStructuredText](https://en.wikipedia.org/wiki/ReStructuredText) format [docstring](https://docs.python.org/3/glossary.html#term-docstring).
- Insert or overwrite shebang line on file head, and manage preset shebang list.
- Supports multiple languages.

## Usage

### Generate `__all__`

List all exportable symbols, in order of symbol declaration, by alphabet order, or by character order. The following symbols will not be included

- dunder variables (double under line variables), `__author__` e.g.
- private symbols, such as `_var`, `_func()`, `_InnerClass`.
- variables unpacked on `for` loop, such as the `i` in `for i in range(10)` .
- imported symbols.

Gray item indicate the symbol is already in `__all__`, it will not be included even if you choose it.

![Generate Dunder All](./.img/generate-dunder-all.png)

### Optimize `__all__`

Resort the list of `__all__`, change quotes style, change line style, etc.

Optimization options will be remembered and affect the results of the next [Generate `__all__`](#Generate-__all__).

![Optimize Dunder All](./.img/optimize-dunder-all.png)

### Insert hyperlink into docstring

Inserted hyperlink was effect when it rendered by [reStructuredText](https://en.wikipedia.org/wiki/ReStructuredText) format [docstring](https://docs.python.org/3/glossary.html#term-docstring) .

If PyCharm rendering fails, change docstring format on: _File_ → _Settings..._ → _Tools_ → _Python Integrated Tools_ → _Docstrings_

![Insert Hyperlink into Docstring](./.img/insert-docstring-hyperlink.png)

When your mouse hover on symbol or file, or their docstring, PyCharm will pop up a window like:

![带有超链接的文档字符串的效果展示](./.img/hyperlink-in-docstring.png)

### Generate Shebang Line

Provide some common Shebang. You can also generate Shebang from a path base on project root, an absolute path, or any string.

To config preset Shebang list and it's order, open settings panel (by `Control` `Alt` `S`) and find _Tools_ → _Tiny Snake_ → _Shebang list_

![Generate Shebang Line](./.img/generate-shebang.png)

### Convert dict

Convert between literal `dict`

```python
DATABASE = {
    "ENGINE": 'django.db.backends.postgresql',
    "NAME": '<DATABASE-NAME>',
    "USER": 'postgres',
    "PASSWORD": None,
    "HOST": '127.0.0.1',
    "PORT": '5432',
}
```

and `dict()`

```python
DATABASE = dict(
    ENGINE='django.db.backends.postgresql',
    NAME='<DATABASE-NAME>',
    USER='postgres',
    PASSWORD='',
    HOST='127.0.0.1',
    PORT='5432',
)
```

## Translation

Create a file name `TinySnakeBundle_<LNG>.properties` or choose any `properties` file on `./src/main/resources/messages/` .  
The `<LNG>` means language tag, see [List of Common Primary Language Subtags](https://en.wikipedia.org/wiki/IETF_language_tag#List_of_common_primary_language_subtags).

There are two ways to quick start:

1. Open this project on IntelliJ IDEA, install plugin [Resource Bundle Editor](https://plugins.jetbrains.com/plugin/17035-resource-bundle-editor), open the file you just created or opened, toggle mode from _Text_ to _Bundle_ .
2. Copy contents of any properties file, and fill the value with translated text into the file you just created or opened. (Format of content is `key=value`)
